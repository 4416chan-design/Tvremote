package com.example

import com.example.data.model.TvAppCatalog
import com.example.data.model.TvKeyCodes
import com.example.network.ProtobufWire
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ProtobufAndCertUnitTest {

    @Test
    fun `protobuf framing roundtrip preserves Korean UTF-8 IME batch edit`() {
        val koreanText = "넷플릭스 오징어 게임"
        val encoded = ProtobufWire.encode {
            writeMessageField(21) {
                writeVarintField(1, 1)
                writeVarintField(2, 0)
                writeMessageField(3) {
                    writeVarintField(1, 2)
                    writeMessageField(2) {
                        writeVarintField(1, 0)
                        writeVarintField(2, 10000)
                        writeStringField(3, koreanText)
                    }
                }
            }
        }

        val out = ByteArrayOutputStream()
        ProtobufWire.writeFramedMessage(out, encoded)

        val framedBytes = ProtobufWire.readFramedMessage(ByteArrayInputStream(out.toByteArray()))
        val root = ProtobufWire.parseFields(framedBytes)
        val batchEdit = root.first { it.number == 21 }.asMessage()
        val editInfo = batchEdit.first { it.number == 3 }.asMessage()
        val imeObj = editInfo.first { it.number == 2 }.asMessage()
        val decodedText = imeObj.first { it.number == 3 }.asString()

        assertEquals(koreanText, decodedText)
    }

    @Test
    fun `remote key inject encodes keycode and short direction`() {
        val encoded = ProtobufWire.encode {
            writeMessageField(10) {
                writeVarintField(1, TvKeyCodes.MEDIA_FAST_FORWARD)
                writeVarintField(2, 3)
            }
        }
        val fields = ProtobufWire.parseFields(encoded)
        val keyInject = fields.first { it.number == 10 }.asMessage()
        assertEquals(TvKeyCodes.MEDIA_FAST_FORWARD.toLong(), keyInject.first { it.number == 1 }.varint)
        assertEquals(3L, keyInject.first { it.number == 2 }.varint)
    }

    @Test
    fun `remote app link launch encodes safe native scheme without market launch`() {
        val uri = TvAppCatalog.launchUriForPackage("com.google.android.youtube.tv")
        assertEquals("vnd.youtube://", uri)
        assertEquals("market://details?id=com.google.android.youtube.tv", TvAppCatalog.marketUriForPackage("com.google.android.youtube.tv"))
        assertTrue(TvAppCatalog.isLegacyBrokenUri("market://launch?id=com.google.android.youtube.tv"))
        assertTrue(TvAppCatalog.isLegacyBrokenUri("https://www.youtube.com/tv"))
        assertEquals(false, TvAppCatalog.isLegacyBrokenUri("vnd.youtube://"))
        val encoded = ProtobufWire.encode {
            writeMessageField(90) {
                writeStringField(1, uri)
            }
        }
        val fields = ProtobufWire.parseFields(encoded)
        assertTrue(fields.any { it.number == 90 })
        val appLink = fields.first { it.number == 90 }.asMessage()
        assertEquals(uri, appLink.first { it.number == 1 }.asString())
    }

    @Test
    fun `negative int32 varint encodes as 64-bit sign-extended two complement`() {
        val encoded = ProtobufWire.encode {
            writeVarintField(1, -1)
        }
        // tag (1 << 3 | 0 = 0x08) + 10-byte two's complement varint for -1L
        assertEquals(11, encoded.size)
        val parsed = ProtobufWire.parseFields(encoded)
        assertEquals(-1L, parsed.first { it.number == 1 }.varint)
    }

    @Test
    fun `adb key manager produces exact 524-byte android rsa struct and 256-byte signature`() {
        val tempDir = java.io.File.createTempFile("adb_test", "").apply {
            delete()
            mkdirs()
        }
        try {
            val manager = com.example.network.AdbKeyManager(tempDir)
            val (_, pubKey) = manager.getOrCreateKeyPair()
            val structBytes = com.example.network.AdbKeyManager.buildAndroidRsaPublicKeyStruct(pubKey)
            assertEquals(524, structBytes.size)

            val payloadStr = manager.buildAdbPublicKeyPayload().toString(Charsets.US_ASCII)
            assertTrue(payloadStr.contains(" tvremote@android\u0000"))

            val token = ByteArray(20) { it.toByte() }
            val sig = manager.signToken(token)
            assertEquals(256, sig.size)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `tv remote keycodes match Android KeyEvent constants`() {
        assertEquals(284, TvKeyCodes.ALL_APPS)
        assertEquals(176, TvKeyCodes.SETTINGS)
        assertEquals(220, TvKeyCodes.BRIGHTNESS_DOWN)
        assertEquals(221, TvKeyCodes.BRIGHTNESS_UP)
    }

    @Test
    fun `remote set volume level parses max level and muted from fields 6 7 8`() {
        val unmutedPayload = ProtobufWire.encode {
            writeMessageField(50) {
                writeVarintField(6, 100) // volume_max
                writeVarintField(7, 18)  // volume_level
                writeVarintField(8, 0)   // volume_muted = false
            }
        }
        val unmutedMsg = ProtobufWire.parseFields(unmutedPayload).first { it.number == 50 }.asMessage()
        val (level, max, muted) = com.example.network.AndroidTvRemoteClient.parseVolumeLevel(unmutedMsg)
        assertEquals(18, level)
        assertEquals(100, max)
        assertEquals(false, muted)

        val mutedPayload = ProtobufWire.encode {
            writeMessageField(50) {
                writeVarintField(6, 100)
                writeVarintField(7, 18)
                writeVarintField(8, 1)   // volume_muted = true
            }
        }
        val mutedMsg = ProtobufWire.parseFields(mutedPayload).first { it.number == 50 }.asMessage()
        val (mLevel, mMax, mMuted) = com.example.network.AndroidTvRemoteClient.parseVolumeLevel(mutedMsg)
        assertEquals(18, mLevel)
        assertEquals(100, mMax)
        assertEquals(true, mMuted)
    }

    @Test
    fun `ime batch edit payload uses insert zero and preserves counters and range`() {
        val payload = com.example.network.AndroidTvRemoteClient.buildImeBatchEditPayload(
            imeCounter = 0,
            fieldCounter = 3,
            start = 0,
            end = 4,
            text = "유튜브 검색",
            insert = 0
        )
        val root = ProtobufWire.parseFields(payload)
        val batchEdit = root.first { it.number == 21 }.asMessage()
        assertEquals(0L, batchEdit.first { it.number == 1 }.varint)
        assertEquals(3L, batchEdit.first { it.number == 2 }.varint)

        val editInfo = batchEdit.first { it.number == 3 }.asMessage()
        assertEquals(0L, editInfo.first { it.number == 1 }.varint)

        val imeObj = editInfo.first { it.number == 2 }.asMessage()
        assertEquals(0L, imeObj.first { it.number == 1 }.varint)
        assertEquals(4L, imeObj.first { it.number == 2 }.varint)
        assertEquals("유튜브 검색", imeObj.first { it.number == 3 }.asString())
    }

    @Test
    fun `apply text edit handles full replacement and range edits safely`() {
        assertEquals(
            "새 검색어",
            com.example.network.AndroidTvRemoteClient.applyTextEdit("기존", 0, 2, "새 검색어")
        )
        assertEquals(
            "abXYef",
            com.example.network.AndroidTvRemoteClient.applyTextEdit("abcdef", 2, 4, "XY")
        )
    }

    @Test
    fun `adb app launcher builds safe shell command and rejects injection`() {
        assertEquals(null, com.example.network.NetworkAdbClient.sanitizePackageName("bad; rm -rf /"))
        assertEquals("com.google.android.youtube.tv", com.example.network.NetworkAdbClient.sanitizePackageName(" com.google.android.youtube.tv "))

        val cmd = com.example.network.NetworkAdbClient.buildAppLaunchShellCommand("com.google.android.youtube.tv")!!
        assertTrue(cmd.contains("LEANBACK_LAUNCHER"))
        assertTrue(cmd.contains("market://details?id=\$PKG"))
        assertTrue(cmd.contains("STATUS=APP"))
        assertTrue(cmd.contains("STATUS=STORE"))
    }

    @Test
    fun `adb keyevent builder formats KEYCODE_ALL_APPS 284 and rejects invalid codes`() {
        assertEquals(null, com.example.network.NetworkAdbClient.buildKeyEventShellCommand(0))
        assertEquals(null, com.example.network.NetworkAdbClient.buildKeyEventShellCommand(-1))
        assertEquals(
            "input keyevent 284 2>&1",
            com.example.network.NetworkAdbClient.buildKeyEventShellCommand(TvKeyCodes.ALL_APPS)
        )
    }
}
