package dev.drzepka.smarthome.logger.pv.solarman

import dev.drzepka.smarthome.logger.core.util.HexUtils

class SolarmanV5FrameDecodingException(
    val content: ByteArray,
    val reason: String,
    details: String? = null
) : RuntimeException(
    "Error while decoding frame: ${HexUtils.byteArrayToHex(content, true)}. " +
            "Reason: $reason${details ?: ""}"
)
