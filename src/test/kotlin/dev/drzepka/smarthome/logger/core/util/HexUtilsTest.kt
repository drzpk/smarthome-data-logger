package dev.drzepka.smarthome.logger.core.util

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HexUtilsTest {

    @Test
    fun `should convert byte array to hex`() {
        val input = intArrayOf(0x23, 0x01, 0xde, 0xad, 0x00).map(Int::toByte).toByteArray()
        assertEquals("2301dead00", HexUtils.byteArrayToHex(input, false))
        assertEquals("23 01 de ad 00", HexUtils.byteArrayToHex(input, true))
    }

    @Test
    fun `should convert hex string to byte array`() {
        val expected = intArrayOf(0xa5, 0x17, 0x0, 0x10, 0x45, 0x38, 0x0).map(Int::toByte).toByteArray()
        assertArrayEquals(expected, HexUtils.hexToByteArray("a5 17 00 10 45 38 00"))
    }

    @Test
    fun `should convert unsigned number to byte array`() {
        assertArrayEquals(byteArrayOf(12), HexUtils.unsignedNumberToByteArray(12, 1))
        assertArrayEquals(byteArrayOf(0, 106), HexUtils.unsignedNumberToByteArray(106, 2))
        assertArrayEquals(byteArrayOf(0, 1, 42, -88), HexUtils.unsignedNumberToByteArray(76456, 4))
    }
}
