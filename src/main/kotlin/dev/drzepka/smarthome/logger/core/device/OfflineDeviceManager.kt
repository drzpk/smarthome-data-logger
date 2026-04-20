package dev.drzepka.smarthome.logger.core.device

import dev.drzepka.smarthome.common.util.Logger
import dev.drzepka.smarthome.logger.core.config.OfflineDeviceProperties
import dev.drzepka.smarthome.logger.core.model.Device
import dev.drzepka.smarthome.logger.core.model.MacAddress

class OfflineDeviceManager(properties: OfflineDeviceProperties) : DeviceManager {
    private val log by Logger()
    private val devices: Map<MacAddress, Device> = properties.devices.associate { entry ->
        entry.mac to Device(id = entry.id, type = entry.type, mac = entry.mac.value)
    }

    init {
        log.info("Loaded {} offline device(s)", devices.size)
    }

    override suspend fun initialize() = Unit

    override fun start() = Unit

    override fun stop() = Unit

    override fun getDeviceId(mac: MacAddress): Int? = devices[mac]?.id

    override fun getDevices(): Map<MacAddress, Device> = devices
}
