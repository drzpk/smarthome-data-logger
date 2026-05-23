package dev.drzepka.smarthome.logger.core.device

import dev.drzepka.smarthome.logger.core.model.Device
import dev.drzepka.smarthome.logger.core.model.MacAddress
import dev.drzepka.smarthome.logger.core.util.Mockable

@Mockable
interface DeviceManager {
    suspend fun initialize()
    fun start()
    fun stop()
    fun getDeviceId(mac: MacAddress): Int?
    fun getDevices(): Map<MacAddress, Device>
}
