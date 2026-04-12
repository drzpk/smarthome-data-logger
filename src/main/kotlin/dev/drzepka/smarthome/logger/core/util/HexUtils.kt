package dev.drzepka.smarthome.logger.core.util

@Suppress("MemberVisibilityCanBePrivate")
object HexUtils {
    const val CHARSET = "0123456789abcdef"

    fun byteArrayToHex(array: ByteArray, separateBytes: Boolean = false): String = array
        .map(Byte::toInt)
        .flatMap { listOf((it shr 4) and 0xf, it and 0xf) }
        .map { CHARSET[it] }
        .chunked(2)
        .joinToString(separator = "") {
            it.joinToString(separator = "", postfix = if (separateBytes) " " else "")
        }
        .trim()

    fun hexToByteArray(hexString: String): ByteArray {
        val sanitized = hexString.sanitize()
        if (sanitized.length % 2 != 0)
            throw IllegalArgumentException("Invalid input length")

        return sanitized
            .chunked(2)
            .map { hexToByte(it) }
            .toByteArray()
    }

    fun hexToByte(hexString: String): Byte {
        val sanitized = hexString.sanitize()
        if (sanitized.length > 2)
            throw IllegalArgumentException("Invalid input length")

        val high = if (sanitized.length > 1) CHARSET.indexOf(sanitized[0]) else 0
        val low = if (sanitized.isNotEmpty()) CHARSET.indexOf(sanitized[1]) else 0
        if (high < 0 || low < 0)
            throw IllegalArgumentException("Invalid input string")

        return (high * 16 + low).toByte()
    }

    fun unsignedNumberToByteArray(number: Number, size: Int): ByteArray {
        if (size < 1 || size > 8)
            throw IllegalArgumentException("Invalid size")

        val longNumber = number.toLong()
        if (longNumber < 0)
            throw IllegalArgumentException("Number must be positive")


        val bytes = ByteArray(size)
        for (i in 0 until size) {
            bytes[i] = (longNumber shr (8 * (size - i - 1))).toByte()
        }
        return bytes
    }

    private fun String.sanitize(): String = lowercase().replace(" ", "").replace("0x", "")
}
