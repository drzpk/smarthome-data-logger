package dev.drzepka.smarthome.logger.sensors.model.server

import java.io.Serializable
import java.math.BigDecimal

class Measurement : Serializable {
    var deviceId = 0
    var temperature: BigDecimal = BigDecimal.ZERO
    var humidity: BigDecimal = BigDecimal.ZERO
    var batteryVoltage: BigDecimal? = null
    var batteryLevel: Int? = null
    var timestampOffsetMillis: Long? = null
}