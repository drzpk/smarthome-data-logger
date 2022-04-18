package dev.drzepka.smarthome.logger.sensors

import com.diozero.api.I2CDevice
import com.diozero.api.I2CDeviceInterface
import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.DataSource
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.FixedRateDataSource
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.ListenerDataSource
import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.model.LocalMeasurement
import dev.drzepka.smarthome.logger.sensors.model.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.Device
import dev.drzepka.smarthome.logger.sensors.pipeline.collector.MockSHTC3DataCollector
import dev.drzepka.smarthome.logger.sensors.pipeline.collector.SHTC3DataCollector
import dev.drzepka.smarthome.logger.sensors.pipeline.decoder.BluetoothServiceDataDecoder
import dev.drzepka.smarthome.logger.sensors.pipeline.decoder.SHTC3Decoder
import dev.drzepka.smarthome.logger.sensors.pipeline.listener.bluetooth.BluetoothCtlBluetoothListener
import dev.drzepka.smarthome.logger.sensors.pipeline.listener.bluetooth.MockBluetoothListener
import java.time.Duration
import java.util.regex.Pattern

class DataSourceFactory(private val deviceManager: DeviceManager, private val useMocks: Boolean) {
    private val log by Logger()

    fun createDataSources(): List<DataSource<*, LocalMeasurement>> {
        val devicesByType = deviceManager.getDevices().values.groupBy { it.type }
        val dataSources = mutableListOf<DataSource<*, LocalMeasurement>>()

        for (type in devicesByType.keys) {
            log.info("Creating data source for type: {}", type)

            val dataSource = when (type) {
                DEVICE_TYPE_XIAOMI_THERMOMETER -> createXiaomiThermometerDataSource()
                DEVICE_TYPE_SHTC3 -> createSHTC3DataSource(devicesByType)
                else -> {
                    log.warn("Unknown data source type: {}", type)
                    null
                }
            }

            if (dataSource != null)
                dataSources.add(dataSource)
        }

        return dataSources
    }

    private fun createXiaomiThermometerDataSource(): DataSource<*, LocalMeasurement> {
        val listener = if (useMocks) MockBluetoothListener() else BluetoothCtlBluetoothListener()
        return ListenerDataSource("bluetooth", listener, BluetoothServiceDataDecoder)
    }

    private fun createSHTC3DataSource(devicesByType: Map<String, List<Device>>): DataSource<*, LocalMeasurement>? {
        val shtc3Devices = devicesByType[DEVICE_TYPE_SHTC3]!!
        if (shtc3Devices.count() > 1) {
            log.error("Unable to create device of type {}. Only one device of this type can be connected at the time.")
            return null
        }

        val name = "shtc3"
        val interval = Duration.ofSeconds(35) // todo: make this configurable
        return if (!useMocks) {
            val mac = MacAddress(shtc3Devices.first().mac)
            val device = createSHTC3i2cDevice(mac.value) ?: return null
            FixedRateDataSource(name, interval, SHTC3DataCollector(device, mac), SHTC3Decoder)
        } else {
            FixedRateDataSource(name, interval, MockSHTC3DataCollector, DataDecoder.noop())
        }
    }

    private fun createSHTC3i2cDevice(mac: String): I2CDeviceInterface? {
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

    companion object {
        const val DEVICE_TYPE_XIAOMI_THERMOMETER = "LYWSD03MMC"
        const val DEVICE_TYPE_SHTC3 = "SHTC3"
    }
}
