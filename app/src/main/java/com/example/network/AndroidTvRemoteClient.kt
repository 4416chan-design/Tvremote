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
    private val onRemoteError: (code: Int, detail: String?) -> Unit = { _, _ -> },
    private val onDisconnected: (Throwable?) -> Unit
) {

    private var socket: SSLSocket? = null
    private var output: OutputStream? = null
    private var readJob: Job? = null
    private var sessionScope: CoroutineScope? = null
    private val writeMutex = Mutex()
    private val imeMutex = Mutex()

    @Volatile
    private var imeCounter: Int = 0

    @Volatile
    private var fieldCounter: Int = 0

    @Volatile
    private var trackedTvText: String = ""

    @Volatile
    var isConnected: Boolean = false
        private set

    suspend fun connect(host: String, remotePort: Int) = withContext(Dispatchers.IO) {
        disconnect(notify = false)
        imeCounter = 0
        fieldCounter = 0
        trackedTvText = ""

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

        // Perform initial RemoteConfigure handshake (include PING|KEY|IME|POWER|VOLUME|APP_LINK)
        val serverConfigFrame = ProtobufWire.readFramedMessage(input)
        val serverFields = ProtobufWire.parseFields(serverConfigFrame)
        if (serverFields.none { it.number == 1 }) {
            throw IllegalStateException("TV에서 페어링 인증이 필요합니다.")
        }

        val configurePayload = ProtobufWire.encode {
            writeMessageField(1) { // remote_configure
                writeVarintField(1, REMOTE_FEATURES)
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
                    writeVarintField(1, REMOTE_FEATURES)
                }
            }
            sendRaw(activeReply)
        }
        handleIncomingFields(activeFields)

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
                handleIncomingFields(fields)
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

    private suspend fun handleIncomingFields(fields: List<ProtobufWire.Field>) {
        for (field in fields) {
            when (field.number) {
                3 -> { // remote_error
                    val errFields = field.asMessage()
                    val code = errFields.firstOrNull { it.number == 1 }?.varint?.toInt() ?: -1
                    val detail = errFields.firstOrNull { it.number == 2 }?.bytes?.let {
                        runCatching {
                            val nested = ProtobufWire.parseFields(it)
                            nested.firstOrNull { f -> f.number == 1 || f.number == 2 }?.asString()
                        }.getOrNull() ?: it.toString(Charsets.UTF_8)
                    }
                    onRemoteError(code, detail)
                }
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
                        syncImeTextStatus(textStatus, isShowRequest = false)
                    }
                }
                21 -> { // remote_ime_batch_edit (TV echoes/acknowledges updated ime_counter, field_counter & text)
                    val batchFields = field.asMessage()
                    syncIncomingImeBatchEdit(batchFields)
                }
                22 -> { // remote_ime_show_request
                    val showFields = field.asMessage()
                    val textStatus = showFields.firstOrNull { it.number == 2 }?.asMessage()
                    if (textStatus != null) {
                        syncImeTextStatus(textStatus, isShowRequest = true)
                    } else {
                        imeCounter = 0
                        trackedTvText = ""
                    }
                    onImeShowRequested(fieldCounter, trackedTvText)
                }
                50 -> { // remote_set_volume_level (6=volume_max, 7=volume_level, 8=volume_muted)
                    val (level, max, muted) = parseVolumeLevel(field.asMessage())
                    onVolumeChanged(level, max, muted)
                }
            }
        }
    }

    private fun syncIncomingImeBatchEdit(batchFields: List<ProtobufWire.Field>) {
        batchFields.firstOrNull { it.number == 1 }?.varint?.toInt()?.let { tvImeCounter ->
            imeCounter = tvImeCounter
        }
        batchFields.firstOrNull { it.number == 2 }?.varint?.toInt()?.let { tvFieldCounter ->
            fieldCounter = tvFieldCounter
        }
        val edits = batchFields.filter { it.number == 3 }
        for (editField in edits) {
            val editMsg = editField.asMessage()
            val imeObj = editMsg.firstOrNull { it.number == 2 }?.asMessage() ?: continue
            val value = imeObj.firstOrNull { it.number == 3 }?.asString() ?: continue
            val start = imeObj.firstOrNull { it.number == 1 }?.varint?.toInt()
            val end = imeObj.firstOrNull { it.number == 2 }?.varint?.toInt()
            trackedTvText = applyTextEdit(trackedTvText, start, end, value)
        }
    }

    private fun syncImeTextStatus(textStatus: List<ProtobufWire.Field>, isShowRequest: Boolean) {
        val incomingFc = textStatus.firstOrNull { it.number == 1 }?.varint?.toInt()
        val incomingValue = textStatus.firstOrNull { it.number == 2 }?.asString()
        val incomingInt5 = textStatus.firstOrNull { it.number == 5 }?.varint?.toInt()

        val fieldChanged = incomingFc != null && incomingFc != fieldCounter
        if (incomingFc != null) {
            fieldCounter = incomingFc
        }

        if (isShowRequest || fieldChanged) {
            // Fresh input field (or explicit show request): proto3 omits empty string / 0 counter on wire
            trackedTvText = incomingValue ?: ""
            imeCounter = incomingInt5 ?: 0
        } else {
            if (incomingValue != null) {
                trackedTvText = incomingValue
            }
            if (incomingInt5 != null) {
                imeCounter = incomingInt5
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
     * Sends UTF-8 text (Korean Hangul & English) to TV IME via serialized RemoteImeBatchEdit.
     * Matches Android TV Remote v2 wire format: RemoteEditInfo.insert = 0, [start, end) selection
     * bounds synced with trackedTvText, and imeCounter matched/incremented per edit.
     */
    suspend fun sendText(text: String, replaceExisting: Boolean = false, submitWithEnter: Boolean = false) {
        imeMutex.withLock {
            val counter = imeCounter
            val fc = fieldCounter
            val currentLength = trackedTvText.length
            val startPos = if (replaceExisting) 0 else currentLength
            val endPos = currentLength

            val payload = buildImeBatchEditPayload(
                imeCounter = counter,
                fieldCounter = fc,
                start = startPos,
                end = endPos,
                text = text,
                insert = 0
            )
            sendRaw(payload)
            imeCounter = counter + 1
            trackedTvText = if (replaceExisting) text else trackedTvText + text

            if (submitWithEnter) {
                kotlinx.coroutines.delay(150)
                sendKey(TvKeyCodes.ENTER, DIRECTION_SHORT)
            }
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
        const val REMOTE_FEATURES = 623 // PING(1)|KEY(2)|IME(4)|POWER(8)|VOLUME(32)|64|APP_LINK(512)

        fun parseVolumeLevel(volFields: List<ProtobufWire.Field>): Triple<Int, Int, Boolean> {
            val max = (volFields.firstOrNull { it.number == 6 }?.varint?.toInt() ?: 100).coerceAtLeast(1)
            val level = (volFields.firstOrNull { it.number == 7 }?.varint?.toInt() ?: 0).coerceIn(0, max)
            val muted = (volFields.firstOrNull { it.number == 8 }?.varint ?: 0L) != 0L
            return Triple(level, max, muted)
        }

        fun buildImeBatchEditPayload(
            imeCounter: Int,
            fieldCounter: Int,
            start: Int,
            end: Int,
            text: String,
            insert: Int = 0
        ): ByteArray = ProtobufWire.encode {
            writeMessageField(21) { // remote_ime_batch_edit
                writeVarintField(1, imeCounter)
                writeVarintField(2, fieldCounter)
                writeMessageField(3) { // edit_info (RemoteEditInfo)
                    writeVarintField(1, insert)
                    writeMessageField(2) { // text_field_status (RemoteImeObject)
                        writeVarintField(1, start)
                        writeVarintField(2, end)
                        writeStringField(3, text)
                    }
                }
            }
        }

        fun applyTextEdit(existing: String, start: Int?, end: Int?, replacement: String): String {
            if (start == null || end == null || start < 0 || end < start || start > existing.length) {
                return replacement
            }
            val safeEnd = end.coerceIn(start, existing.length)
            return existing.substring(0, start) + replacement + existing.substring(safeEnd)
        }
    }
}
