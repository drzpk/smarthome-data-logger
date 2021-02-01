package dev.drzepka.smarthome.logger.model.config

import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType

enum class SourceType(val deviceType: DeviceType) {
    SMA(DeviceType.SMA),
    SOFAR_WIFI(DeviceType.SOFAR),
    SOFAR_MODBUS(DeviceType.SOFAR)
}