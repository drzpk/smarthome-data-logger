package dev.drzepka.smarthome.logger.sensors.model.bluetooth

class BluetoothServiceData(val mac: MacAddress, hex: String) : BluetoothData {
    val data: ByteArray = hexToByteArray(hex)

    private fun hexToByteArray(hex: String): ByteArray {
        val byteList = ArrayList<Byte>()

        var buffer = ""
        for (char in hex) {
            if (isWhitespace(char))
                continue

            buffer += char
            if (buffer.length == 2) {
                byteList.add(hexToByte(buffer.toLowerCase()))
                buffer = ""
            }
        }

        return byteList.toByteArray()
    }

    private fun isWhitespace(char: Char): Boolean = char == ' ' || char == '\t'

    private fun hexToByte(hex: String): Byte {
        val left = HEX_CHARSET.indexOf(hex[0])
        val right = HEX_CHARSET.indexOf(hex[1])
        if (left == -1 || right == -1)
            throw IllegalArgumentException("'$hex' is not valid hex byte")

        return (left * 16 + right).toByte()
    }

    companion object {
        private const val HEX_CHARSET = "0123456789abcdef"
    }
}