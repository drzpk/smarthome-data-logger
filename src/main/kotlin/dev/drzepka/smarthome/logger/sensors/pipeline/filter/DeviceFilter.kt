package dev.drzepka.smarthome.logger.sensors.pipeline.filter

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.core.pipeline.component.DataFilter
import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.model.LocalMeasurement

class DeviceFilter(private val deviceManager: DeviceManager) : DataFilter<LocalMeasurement> {
    private val log by Logger()

    override fun start(context: PipelineContext) {
        super.start(context)
        deviceManager.start(context)
    }

    override fun stop(context: PipelineContext) {
        super.stop(context)
        deviceManager.stop(context)
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
