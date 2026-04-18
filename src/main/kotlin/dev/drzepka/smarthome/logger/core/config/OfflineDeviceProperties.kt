package dev.drzepka.smarthome.logger.core.config

import dev.drzepka.smarthome.logger.core.model.MacAddress

class OfflineDeviceProperties(source: ConfigPropertySource) {
    val enabled = source.getBoolean("sensors.offline.enabled", false)
    val devices: List<OfflineDevice> = source.getKeys("sensors.offline.devices").map { index ->
        OfflineDevice(index.toInt(), source.getChild("sensors.offline.devices.$index"))
    }
}

class OfflineDevice(val id: Int, source: ConfigPropertySource) {
    val mac = MacAddress(source.getString("mac"))
    val type = source.getString("type")
}
