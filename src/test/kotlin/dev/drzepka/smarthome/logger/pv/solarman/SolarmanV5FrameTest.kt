package dev.drzepka.smarthome.logger.pv.solarman

import dev.drzepka.smarthome.logger.core.frame.Frame
import dev.drzepka.smarthome.logger.core.util.HexUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class SolarmanV5FrameTest {

    @Test
    fun `should encode frame`() {
        val expected =
            HexUtils.hexToByteArray("a5 17 00 10 45 38 00 08 77 6f d1 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 03 00 00 00 0a c5 cd 05 15")
        val frame = SolarmanV5Frame(56, 3513743112L, TestFrame())
        Assertions.assertArrayEquals(expected, frame.encodeRequest())
    }

    @Test
    fun `should decode frame`() {
        // Response from loading 2 input registers starting at 1014
        val rawResponse =
            HexUtils.hexToByteArray("a5 17 00 10 15 38 58 08 77 6f d1 02 01 fa dc 3a 32 9b 09 00 00 80 5b 21 34 01 04 04 00 00 27 0d 21 b1 b3 15")

        val frame = SolarmanV5Frame(56, 3513743112L, TestFrame())
        val decoded = frame.decodeResponse(rawResponse)

        Assertions.assertTrue(decoded is ByteArray)
        Assertions.assertArrayEquals(HexUtils.hexToByteArray("01 04 04 00 00 27 0d 21 b1"), decoded as ByteArray)
    }

    @Test
    fun `should reject response with incorrect checksum`() {
        // Corrupt byte[34] (checksum) from b3 to b4
        val raw = HexUtils.hexToByteArray("a5 17 00 10 15 38 58 08 77 6f d1 02 01 fa dc 3a 32 9b 09 00 00 80 5b 21 34 01 04 04 00 00 27 0d 21 b1 b4 15")
        val frame = SolarmanV5Frame(56, 3513743112L, TestFrame())

        val ex = Assertions.assertThrows(SolarmanV5FrameDecodingException::class.java) { frame.decodeResponse(raw) }
        Assertions.assertTrue(ex.reason.contains("checksum"))
    }

    @Test
    fun `should reject response with incorrect serial (request echo)`() {
        // Corrupt byte[5] (requestEcho) from 38 to 39
        val raw = HexUtils.hexToByteArray("a5 17 00 10 15 39 58 08 77 6f d1 02 01 fa dc 3a 32 9b 09 00 00 80 5b 21 34 01 04 04 00 00 27 0d 21 b1 b3 15")
        val frame = SolarmanV5Frame(56, 3513743112L, TestFrame())

        val ex = Assertions.assertThrows(SolarmanV5FrameDecodingException::class.java) { frame.decodeResponse(raw) }
        Assertions.assertTrue(ex.reason.contains("request echo"))
    }

    @Test
    fun `should reject response with incorrect serial (response echo)`() {
        // Corrupt bytes[7-10] (device SN) — change 08 to 09
        val raw = HexUtils.hexToByteArray("a5 17 00 10 15 38 58 09 77 6f d1 02 01 fa dc 3a 32 9b 09 00 00 80 5b 21 34 01 04 04 00 00 27 0d 21 b1 b3 15")
        val frame = SolarmanV5Frame(56, 3513743112L, TestFrame())

        val ex = Assertions.assertThrows(SolarmanV5FrameDecodingException::class.java) { frame.decodeResponse(raw) }
        Assertions.assertTrue(ex.reason.contains("logger serial"))
    }

    @Test
    fun `should reject response with incorrect length`() {
        // Change payload length field bytes[1-2] from 17 00 (23) to 18 00 (24)
        // making the frame claim a total length of 11+24+2=37 while actual is 36
        val raw = HexUtils.hexToByteArray("a5 18 00 10 15 38 58 08 77 6f d1 02 01 fa dc 3a 32 9b 09 00 00 80 5b 21 34 01 04 04 00 00 27 0d 21 b1 b3 15")
        val frame = SolarmanV5Frame(56, 3513743112L, TestFrame())

        val ex = Assertions.assertThrows(SolarmanV5FrameDecodingException::class.java) { frame.decodeResponse(raw) }
        Assertions.assertTrue(ex.reason.contains("length"))
    }
}

private class TestFrame : Frame<Any> {
    override fun encodeRequest(): ByteArray {
        // An example Modbus function 03 request (https://www.modbustools.com/modbus.html)
        return HexUtils.hexToByteArray("01 03 00 00 00 0a c5 cd")
    }

    override fun decodeResponse(content: ByteArray): Any = content

}
