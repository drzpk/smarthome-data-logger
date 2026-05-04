package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.frame.Frame

/**
 * Inner Modbus frame for Sofar WiFi logger, wrapped inside a SolarmanV5Frame.
 *
 * Encodes a Modbus RTU read holding registers request (slave=1, start=0, qty=39).
 * Decodes the Modbus response by stripping the slave address and function code bytes
 * so the payload aligns with SofarFrame's 1-indexed byte offsets.
 */
class SofarWifiFrame : Frame<SofarData> {

    override fun encodeRequest(): ByteArray = REQUEST.clone()

    override fun decodeResponse(content: ByteArray): SofarData? {
        // content layout from SolarmanV5: [slave_addr, func_code, byte_count, data..., crc(2)]
        // SofarFrame expects: [byte_count, data...] (1-indexed from byte_count)
        val minSize = 3 + 2  // slave + func + byte_count + at least some data + CRC
        if (content.size < minSize) return null

        val raw = content.sliceArray(2 until content.size - 2)
        return SofarFrame().decodeResponse(raw)
    }

    companion object {
        // Modbus RTU: slave=1, func=3 (read holding), start=0x0000, qty=39 (0x27)
        // CRC-16/Modbus of [0x01, 0x03, 0x00, 0x00, 0x00, 0x27] = 0xD005 → bytes [0x05, 0xD0]
        private val REQUEST = byteArrayOf(0x01, 0x03, 0x00, 0x00, 0x00, 0x27, 0x05, 0xD0.toByte())
    }
}
