package dev.drzepka.smarthome.logger.sensors.bluetooth.bluetoothctl

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.ArrayDeque

class InputReaderTest {

    @Test
    fun `should divide input into tokens`() {
        val input = """
            Controller XV:FF:QQ:42:3F:FX (public)
                    Name: raspberrypi
                    Alias: raspberrypi
                    Class: 0x00480000
                    Powered: yes
                    Discoverable: no
                    Pairable: yes
                    UUID: Headset AG                (00001112-0000-1000-8000-00805f9b34fb)
                    UUID: Generic Attribute Profile (00001801-0000-1000-8000-00805f9b34fb)
                    UUID: A/V Remote Control        (0000110e-0000-1000-8000-00805f9b34fb)
                    UUID: Generic Access Profile    (00001800-0000-1000-8000-00805f9b34fb)
                    UUID: PnP Information           (00001200-0000-1000-8000-00805f9b34fb)
                    UUID: A/V Remote Control Target (0000110c-0000-1000-8000-00805f9b34fb)
                    UUID: Audio Source              (0000110a-0000-1000-8000-00805f9b34fb)
                    UUID: Handsfree Audio Gateway   (0000111f-0000-1000-8000-00805f9b34fb)
                    Modalias: usb:v1D6Bp0246d0532
                    Discovering: yes
            [CHG] Device A4:C1:38:BB:1D:CC RSSI: -77
            [CHG] Device DD:C1:31:BB:1D:FF ServiceData Value:
              30 58 5b 05 d9 69 1d 5c 38 c1 a4 08              0X[..i.\8...
            [NEW] Device FF:1A:7D:DF:AB:13 DR-DESKTOP
            [END]
        """.trimIndent()

        val stream = ByteArrayInputStream(input.toByteArray())
        val reader = InputReader(stream)

        val tokens = reader.readTokens()

        // Last line can't be read because it's unknown whether it belong to a token
        // that isn't completly available in the stream yet.
        then(tokens).hasSize(4)

        then(tokens[0].lines()).hasSize(17)
        then(tokens[0]).startsWith("Controller XV:FF:QQ:42:3F:FX (public)\n")
        then(tokens[1]).isEqualTo("[CHG] Device A4:C1:38:BB:1D:CC RSSI: -77")
        then(tokens[2].lines()).hasSize(2)
        then(tokens[2].lines()[0]).isEqualTo("[CHG] Device DD:C1:31:BB:1D:FF ServiceData Value:")
        then(tokens[2].lines()[1]).isEqualTo("  30 58 5b 05 d9 69 1d 5c 38 c1 a4 08              0X[..i.\\8...")
        then(tokens[3]).isEqualTo("[NEW] Device FF:1A:7D:DF:AB:13 DR-DESKTOP")
    }

    @Test
    fun `should read token in parts`() {
        val input1 = """
             some token
             Controller XV:FF:QQ:42:3F:FX (public)
                    Name: raspberrypi
                    Alias: raspberrypi
                    Class: 0x00480000
                    Powered: yes
        """.trimIndent()
        val input2 = """
                    Discoverable: no
                    Pairable: yes
             [NEW] next token       
        """.trimIndent()

        val testStream = TestStream()
        val reader = InputReader(testStream)

        var tokens = reader.readTokens()
        then(tokens).isEmpty()

        testStream.addContent(input1)
        tokens = reader.readTokens()
        then(tokens).hasSize(1)
        then(tokens[0]).isEqualTo("some token")

        testStream.addContent(input2)
        tokens = reader.readTokens()
        then(tokens).hasSize(1)
        then(tokens[0].lines()).hasSize(7)
    }

    private class TestStream : InputStream() {
        private val content = ArrayDeque<Byte>()

        fun addContent(newContent: String) {
            content.addAll(newContent.toByteArray(StandardCharsets.UTF_8).asList())
        }

        override fun read(): Int {
            return if (content.isNotEmpty())
                content.removeFirst().toInt()
            else
                -1
        }

        override fun available(): Int {
            return content.size
        }
    }
}