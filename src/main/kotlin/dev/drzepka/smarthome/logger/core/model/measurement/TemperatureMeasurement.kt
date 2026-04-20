package dev.drzepka.smarthome.logger.core.model.measurement

import java.math.BigDecimal

data class TemperatureMeasurement(
    override val mac: String,
    val temperature: BigDecimal,
    val humidity: BigDecimal? = null,
    val batteryVoltage: BigDecimal? = null,
    val batteryLevel: Int? = null
) : Measurement {
    override val type = MeasurementType.TEMPERATURE
}
