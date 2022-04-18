package dev.drzepka.smarthome.logger.sensors

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.DataSource
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.ListenerDataSource
import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.model.LocalMeasurement
import dev.drzepka.smarthome.logger.sensors.pipeline.decoder.BluetoothServiceDataDecoder
import dev.drzepka.smarthome.logger.sensors.pipeline.listener.bluetooth.BluetoothCtlBluetoothListener
import dev.drzepka.smarthome.logger.sensors.pipeline.listener.bluetooth.MockBluetoothListener

class DataSourceFactory(private val deviceManager: DeviceManager, private val useMocks: Boolean) {
    private val log by Logger()

    fun createDataSources(): List<DataSource<*, LocalMeasurement>> {
        val devicesByType = deviceManager.getDevices().values.groupBy { it.type }
        val dataSources = mutableListOf<DataSource<*, LocalMeasurement>>()

        for (type in devicesByType.keys) {
            log.info("Creating data source for type: {}", type)

            val dataSource = when (type) {
                DEVICE_TYPE_XIAOMI_THERMOMETER -> createXiaomiThermometerDataSource()
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

    companion object {
        const val DEVICE_TYPE_XIAOMI_THERMOMETER = "LYWSD03MMC"
    }
}
