package dev.drzepka.smarthome.logger.sensors.pipeline.collector

import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.sensors.model.LocalMeasurement
import dev.drzepka.smarthome.logger.sensors.model.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.Measurement
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.random.Random

object MockSHTC3DataCollector : DataCollector<LocalMeasurement> {

    override fun getData(): Collection<LocalMeasurement> {
        val temperature = Random.nextDouble(15.0, 25.0)
        val humidity = Random.nextDouble(10.0, 100.0)

        val measurement = Measurement().apply {
            this.temperature = BigDecimal.valueOf(temperature).setScale(1, RoundingMode.HALF_UP)
            this.humidity = BigDecimal.valueOf(humidity).setScale(0, RoundingMode.HALF_UP)
        }

        return listOf(LocalMeasurement(MacAddress("1:70"), measurement))
    }
}
