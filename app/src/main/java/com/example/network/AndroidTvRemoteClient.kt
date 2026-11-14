package com.example.network

import android.os.Build
import com.example.data.model.TvKeyCodes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSocket

/**
 * Maintains the mTLS remote control session on TCP port 6466,
 * handling configure/active handshake, ping heartbeats, key injection,
 * app link launches, and Korean/English IME text batch edits.
 */
class AndroidTvRemoteClient(
    private val certStore: ClientCertStore,
    private val onForegroundAppChanged: (String) -> Unit,
    private val onImeShowRequested: (fieldCounter: Int, text: String) -> Unit,
    private val onVolumeChanged: (level: Int, max: Int, muted: Boolean) -> Unit,
    private val onDisconnected: (Throwable?) -> Unit
) {

    private var socket: SSLSocket? = null
    private var output: OutputStream? = null
    private var readJob: Job? = null
    private var sessionScope: CoroutineScope? = null
    private val writeMutex = Mutex()

    @Volatile
    private var imeCounter: Int = 0

    @Volatile
    private var fieldCounter: Int = 0

    @Volatile
    var isConnected: Boolean = false
        private set

    suspend fun connect(host: String, remotePort: Int) = withContext(Dispatchers.IO) {
        disconnect(notify = false)

        val sslContext = certStore.createSslContext()
        val sslSocket = (sslContext.socketFactory.createSocket() as SSLSocket).apply {
            soTimeout = 30_000
            connect(InetSocketAddress(host, remotePort), 8_000)
            startHandshake()
            soTimeout = 0 // non-blocking reader waits on ping/events
        }

        socket = sslSocket
        output = sslSocket.outputStream
        val input = sslSocket.inputStream

        // Perform initial RemoteConfigure handshake
        val serverConfigFrame = ProtobufWire.readFramedMessage(input)
        val serverFields = ProtobufWire.parseFields(serverConfigFrame)
        if (serverFields.none { it.number == 1 }) {
            throw IllegalStateException("TV에서 페어링 인증이 필요합니다.")
        }

        val configurePayload = ProtobufWire.encode {
            writeMessageField(1) { // remote_configure
                writeVarintField(1, 622)
                writeMessageField(2) { // device_info
                    writeStringField(1, Build.MODEL ?: "Android")
                    writeStringField(2, Build.MANUFACTURER ?: "Google")
                    writeVarintField(3, 1)
                    writeStringField(4, "1")
                    writeStringField(5, "com.aistudio.tvremote")
                    writeStringField(6, "1.0")
                }
            }
        }
        sendRaw(configurePayload)

        // Wait for remote_set_active from TV, then acknowledge
        val activeFrame = ProtobufWire.readFramedMessage(input)
        val activeFields = ProtobufWire.parseFields(activeFrame)
        if (activeFields.any { it.number == 2 }) {
            val activeReply = ProtobufWire.encode {
                writeMessageField(2) {
                    writeVarintField(1, 622)
                }
            }
            sendRaw(activeReply)
        }

        isConnected = true
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        sessionScope = scope
        readJob = scope.launch {
            readLoop(input)
        }
    }

    private suspend fun readLoop(input: InputStream) {
        var disconnectCause: Throwable? = null
        try {
            while (isConnected) {
                val frame = ProtobufWire.readFramedMessage(input)
                val fields = ProtobufWire.parseFields(frame)
                for (field in fields) {
                    when (field.number) {
                        8 -> { // remote_ping_request
                            val pingFields = field.asMessage()
                            val val1 = pingFields.firstOrNull { it.number == 1 }?.varint?.toInt() ?: 0
                            val pong = ProtobufWire.encode {
                                writeMessageField(9) {
                                    writeVarintField(1, val1)
                                }
                            }
                            sendRaw(pong)
                        }
                        20 -> { // remote_ime_key_inject (reports active app & IME status)
                            val imeFields = field.asMessage()
                            val appInfo = imeFields.firstOrNull { it.number == 1 }?.asMessage()
                            val pkg = appInfo?.firstOrNull { it.number == 3 }?.asString()
                            if (!pkg.isNullOrBlank()) {
                                onForegroundAppChanged(pkg)
                            }
                            val textStatus = imeFields.firstOrNull { it.number == 2 }?.asMessage()
                            if (textStatus != null) {
                                fieldCounter = textStatus.firstOrNull { it.number == 1 }?.varint?.toInt() ?: fieldCounter
                            }
                        }
                        22 -> { // remote_ime_show_request
                            val showFields = field.asMessage()
                            val textStatus = showFields.firstOrNull { it.number == 2 }?.asMessage()
                            val fc = textStatus?.firstOrNull { it.number == 1 }?.varint?.toInt() ?: fieldCounter
                            val currentText = textStatus?.firstOrNull { it.number == 2 }?.asString().orEmpty()
                            fieldCounter = fc
                            onImeShowRequested(fc, currentText)
                        }
                        50 -> { // remote_set_volume_level
                            val volFields = field.asMessage()
                            val level = volFields.firstOrNull { it.number == 5 }?.varint?.toInt() ?: 0
                            val max = volFields.firstOrNull { it.number == 6 }?.varint?.toInt() ?: 100
                            val muted = (volFields.firstOrNull { it.number == 7 }?.varint ?: 0L) != 0L
                            onVolumeChanged(level, max, muted)
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            if (isConnected) {
                disconnectCause = if (t is SSLException) {
                    IllegalStateException("페어링 인증서가 만료되었거나 재페어링이 필요합니다.", t)
                } else t
            }
        } finally {
            val wasConnected = isConnected
            isConnected = false
            if (wasConnected) {
                onDisconnected(disconnectCause)
            }
        }
    }

    suspend fun sendKey(keyCode: Int, direction: Int = DIRECTION_SHORT) {
        val payload = ProtobufWire.encode {
            writeMessageField(10) { // remote_key_inject
                writeVarintField(1, keyCode)
                writeVarintField(2, direction)
            }
        }
        sendRaw(payload)
    }

    suspend fun sendPressAndReleaseKey(keyCode: Int) {
        sendKey(keyCode, DIRECTION_START_LONG)
        kotlinx.coroutines.delay(65)
        sendKey(keyCode, DIRECTION_END_LONG)
    }

    suspend fun sendAppLink(appLinkUri: String) {
        val payload = ProtobufWire.encode {
            writeMessageField(90) { // remote_app_link_launch_request
                writeStringField(1, appLinkUri)
            }
        }
        sendRaw(payload)
    }

    /**
     * Sends UTF-8 text (Korean Hangul & English) to TV IME via RemoteImeBatchEdit.
     */
    suspend fun sendText(text: String, replaceExisting: Boolean = false, submitWithEnter: Boolean = false) {
        val counter = ++imeCounter
        val fc = fieldCounter
        val payload = ProtobufWire.encode {
            writeMessageField(21) { // remote_ime_batch_edit
                writeVarintField(1, counter)
                writeVarintField(2, fc)
                writeMessageField(3) { // edit_info
                    writeVarintField(1, if (replaceExisting) 2 else 1)
                    writeMessageField(2) { // RemoteImeObject
                        writeVarintField(1, if (replaceExisting) 0 else -1)
                        writeVarintField(2, if (replaceExisting) 10000 else -1)
                        writeStringField(3, text)
                    }
                }
            }
        }
        sendRaw(payload)
        if (submitWithEnter) {
            sendKey(TvKeyCodes.ENTER)
        }
    }

    private suspend fun sendRaw(payload: ByteArray) = withContext(Dispatchers.IO) {
        writeMutex.withLock {
            val out = output ?: throw IllegalStateException("TV에 연결되어 있지 않습니다.")
            ProtobufWire.writeFramedMessage(out, payload)
        }
    }

    fun disconnect(notify: Boolean = false) {
        val wasConnected = isConnected
        isConnected = false
        readJob?.cancel()
        sessionScope?.cancel()
        readJob = null
        sessionScope = null
        runCatching { socket?.close() }
        socket = null
        output = null
        if (notify && wasConnected) {
            onDisconnected(null)
        }
    }

    companion object {
        const val DIRECTION_START_LONG = 1
        const val DIRECTION_END_LONG = 2
        const val DIRECTION_SHORT = 3
    }
}
