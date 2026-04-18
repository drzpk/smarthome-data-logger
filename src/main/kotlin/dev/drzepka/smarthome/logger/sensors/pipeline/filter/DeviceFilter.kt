package dev.drzepka.smarthome.logger.sensors.pipeline.filter

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.device.DeviceManager
import dev.drzepka.smarthome.logger.core.pipeline.component.DataFilter
import dev.drzepka.smarthome.logger.sensors.model.LocalMeasurement

class DeviceFilter(private val deviceManager: DeviceManager) : DataFilter<LocalMeasurement> {
    private val log by Logger()

    override fun start() {
        deviceManager.start()
    }

    override fun stop() {
        deviceManager.stop()
    }

    override fun filter(data: LocalMeasurement): LocalMeasurement? {
        val id = deviceManager.getDeviceId(data.mac)
        if (id == null) {
            log.trace("No device was found for mac {}", data.mac)
            return null
        }

        log.trace("Device id for mac {} is {}", data.mac, id)
        data.measurement.deviceId = id
        return data
    }
}
