package dev.drzepka.smarthome.logger.sensors.model.server

import java.io.Serializable
import java.math.BigDecimal

class CreateMeasurementsRequest : Serializable {

    var measurements = ArrayList<Measurement>()

    class Measurement : Serializable {
        var deviceId = 0
        var temperature: BigDecimal = BigDecimal.ZERO
        var humidity: BigDecimal = BigDecimal.ZERO
        var batteryVoltage: BigDecimal = BigDecimal.ZERO
        var batteryLevel = 0
        var timestampOffsetMillis: Long? = null
    }
}