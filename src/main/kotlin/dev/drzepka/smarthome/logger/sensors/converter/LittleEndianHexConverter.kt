package dev.drzepka.smarthome.logger.sensors.converter

object LittleEndianHexConverter {

    fun convertToInt16(value: Int): String {
        val array = ByteArray(2)
        array[0] = value.and(0xff).toByte()
        array[1] = value.toShort().div(0xff.toShort()).toByte()

        return encodeToHex(array)
    }

    fun convertToUInt16(value: Int): String {
        val array = ByteArray(2)
        array[0] = value.and(0xff).toByte()
        array[1] = value.shr(8).toByte()

        return encodeToHex(array)
    }

    fun convertToUInt8(value: Int): String {
        val byte = value.and(0xff).toByte()
        return encodeToHex(byteArrayOf(byte))
    }

    private fun encodeToHex(value: ByteArray): String {
        val charset = "0123456789abcdef"
        val builder = StringBuilder()

        for (byte in value) {
            builder.append(charset[byte.toInt().and(0xff).div(16)])
            builder.append(charset[byte.toInt().and(0xff).rem(16)])
        }

        return builder.toString()
    }
}