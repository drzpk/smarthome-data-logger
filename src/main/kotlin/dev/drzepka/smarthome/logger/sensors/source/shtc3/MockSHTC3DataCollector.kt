package dev.drzepka.smarthome.logger.sensors.source.shtc3

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.TemperatureMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

object MockSHTC3DataCollector : DataCollector<Measurement> {

    override suspend fun getData(): Collection<Measurement> {
        val temperature = Random.nextDouble(15.0, 25.0)
        val humidity = Random.nextDouble(10.0, 100.0)

        val measurement = TemperatureMeasurement(
            mac = "1:70",
            temperature = BigDecimal.valueOf(temperature).setScale(1, RoundingMode.HALF_UP),
            humidity = BigDecimal.valueOf(humidity).setScale(0, RoundingMode.HALF_UP)
        )

        return listOf(measurement)
    }
}
