package dev.drzepka.smarthome.logger.pv.solarman

import dev.drzepka.smarthome.logger.core.frame.Frame
import dev.drzepka.smarthome.logger.core.util.HexUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Source: https://pysolarmanv5.readthedocs.io/en/stable/solarmanv5_protocol.html
 *
 * Used by: Afore inverters
 */
class SolarmanV5Frame<T>(
    /** Random number which should be echoed back in the response, used to match requests with responses */
    private val requestEcho: Byte,
    private val deviceSN: Long,
    private val payloadFrame: Frame<T>
) : Frame<T> {

    override fun encodeRequest(): ByteArray {
        val payload = encodePayloadSection()
        val header = encodeHeaderSection(payload.size)
        val encoded = ByteBuffer.allocate(header.size + payload.size + 2)

        encoded.put(header)
        encoded.put(payload)
        encoded.put(calculateChecksum(encoded.array().sliceArray(1..(header.size + payload.size))))
        encoded.put(END_BYTE)

        return encoded.array()
    }

    override fun decodeResponse(content: ByteArray): T? {
        val buffer = ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN)
        checkResponseHeader(buffer)
        checkResponsePayload(buffer)
        checkResponseTrailer(buffer)

        val startIndex = HEADER_LENGTH + RESPONSE_PAYLOAD_BASE_LENGTH
        val endIndex = startIndex + buffer.getShort(1).toInt() - RESPONSE_PAYLOAD_BASE_LENGTH

        val payload = content.sliceArray(startIndex until endIndex)
        return payloadFrame.decodeResponse(payload)
    }

    private fun encodePayloadSection(): ByteArray {
        val encapsulatedPayload = payloadFrame.encodeRequest()
        val buffer = ByteBuffer.allocate(15 + encapsulatedPayload.size).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put(FRAME_TYPE)
        buffer.putShort(SENSOR_TYPE)
        buffer.putInt(TOTAL_WORKING_TIME)
        buffer.putInt(POWER_ON_TIME)
        buffer.putInt(OFFSET_TIME)
        buffer.put(encapsulatedPayload)

        return buffer.array()
    }

    private fun encodeHeaderSection(payloadSize: Int): ByteArray {
        val buffer = ByteBuffer.allocate(11).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put(START_BYTE)
        buffer.putShort(payloadSize.toShort())
        buffer.put(REQUEST_CONTROL_CODE)
        buffer.put(requestEcho)
        buffer.put(0)
        buffer.putInt(deviceSN.toInt())

        return buffer.array()
    }

    private fun checkResponseHeader(buffer: ByteBuffer) {
        assertResponse(buffer, "header too short (${buffer.array().size})", buffer.array().size >= 11)
        assertResponse(buffer, "invalid frame start", START_BYTE, buffer[0])

        val payloadLength = buffer.getShort(1).toInt()
        val expectedTotalLength = 11 + payloadLength + 2
        assertResponse(buffer, "invalid message length", expectedTotalLength, buffer.array().size)

        val responseControlCode = byteArrayOf(buffer[3], buffer[4])
        assertResponse(buffer, "invalid control code", RESPONSE_CONTROL_CODE, responseControlCode)

        // According to the protocol, first half of the sequence number should be echoed back.
        // The second is increased for every response, regardless of whether it was from this frame or not.
        // Because of that it won't be used for validation
        val requestEcho = buffer[5]
        assertResponse(buffer, "invalid request echo", this.requestEcho, requestEcho)

        val responseSN = buffer.getInt(7).toLong() and 0xFFFFFFFFL
        assertResponse(buffer, "invalid logger serial", deviceSN, responseSN)
    }

    private fun checkResponsePayload(buffer: ByteBuffer) {
        assertResponse(buffer, "invalid frame type", 0x2, buffer[HEADER_LENGTH])
        assertResponse(buffer, "invalid status", 0x1, buffer[HEADER_LENGTH + 1])
    }

    private fun checkResponseTrailer(buffer: ByteBuffer) {
        val array = buffer.array()
        val calculatedChecusum = calculateChecksum(array.sliceArray(1 until (array.size - 2)))
        assertResponse(buffer, "invalid checksum", array[array.size - 2], calculatedChecusum)
        assertResponse(buffer, "invalid end", END_BYTE, array[array.size - 1])
    }

    private fun assertResponse(content: ByteBuffer, reason: String, condition: Boolean) {
        if (!condition)
            throw SolarmanV5FrameDecodingException(content.array(), reason)
    }

    private fun <T> assertResponse(content: ByteBuffer, reason: String, expected: T, actual: T) {
        val expectedStr: String
        val actualStr: String
        val isEqual: Boolean
        if (expected is ByteArray && actual is ByteArray) {
            expectedStr = HexUtils.byteArrayToHex(expected, separateBytes = true)
            actualStr = HexUtils.byteArrayToHex(actual, separateBytes = true)
            isEqual = expected.contentEquals(actual)
        } else {
            expectedStr = expected.toString()
            actualStr = actual.toString()
            isEqual = expected == actual
        }

        if (!isEqual)
            throw SolarmanV5FrameDecodingException(
                content = content.array(),
                reason = reason,
                details = " (expected: $expectedStr, actual: $actualStr)"
            )
    }

    private fun calculateChecksum(data: ByteArray): Byte = data.sumOf { it.toInt() }.toByte()

    companion object {
        private const val HEADER_LENGTH = 11
        private const val RESPONSE_PAYLOAD_BASE_LENGTH = 14

        private const val START_BYTE = 0xa5.toByte()
        private const val END_BYTE = 0x15.toByte()

        private const val FRAME_TYPE = 0x02.toByte() // solar inverter
        private const val SENSOR_TYPE = 0.toShort()
        private const val TOTAL_WORKING_TIME = 0
        private const val POWER_ON_TIME = 0
        private const val OFFSET_TIME = 0

        // already encoded in little endian
        private val REQUEST_CONTROL_CODE = HexUtils.hexToByteArray("0x1045")
        private val RESPONSE_CONTROL_CODE = HexUtils.hexToByteArray("0x1015")
    }
}
