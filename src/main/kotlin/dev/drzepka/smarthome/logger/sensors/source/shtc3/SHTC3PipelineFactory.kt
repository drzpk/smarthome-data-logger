package dev.drzepka.smarthome.logger.sensors.source.shtc3

import com.diozero.api.I2CDevice
import com.diozero.api.I2CDeviceInterface
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.model.MacAddress
import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.pipeline.Pipeline
import dev.drzepka.smarthome.logger.core.pipeline.PipelineFactory
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.FixedRateDataSource
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType
import java.time.Duration
import java.util.regex.Pattern

class SHTC3PipelineFactory : PipelineFactory {
    private val log by Logger()

    override val sourceType = SourceType.SHTC3

    override fun create(name: String, properties: ConfigPropertySource): Pipeline? {
        val mock = properties.getBoolean("mock", false)
        val interval = Duration.ofSeconds(properties.getInt("measurementInterval", 35).toLong())

        val dataSource = if (mock) {
            FixedRateDataSource(name, interval, MockSHTC3DataCollector, DataDecoder.noop<Measurement>())
        } else {
            val mac = MacAddress(properties.getString("mac"))
            val device = createI2CDevice(mac.value) ?: return null
            FixedRateDataSource(name, interval, SHTC3DataCollector(device, mac), SHTC3Decoder)
        }

        return Pipeline(name, Duration.ofSeconds(30)).also {
            it.addDataSource(dataSource)
        }
    }

    private fun createI2CDevice(mac: String): I2CDeviceInterface? {
        // Not sure what this does, but with the default value ("true") there are some errors in the log,
        // they don't affect I2C functionality, though.
        System.setProperty("diozero.gpio.chardev", "false")

        val pattern = Pattern.compile("^(\\d):([A-Za-z0-9]{2})\$")
        val matcher = pattern.matcher(mac)
        if (!matcher.matches()) {
            log.error("Mac '{}' doesn't match required pattern: {}", mac, pattern.pattern())
            return null
        }

        val controller = matcher.group(1).toInt()
        val address = matcher.group(2).toInt(16)
        return I2CDevice(controller, address)
    }
}
