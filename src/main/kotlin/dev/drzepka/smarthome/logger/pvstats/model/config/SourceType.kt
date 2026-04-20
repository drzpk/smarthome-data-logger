package dev.drzepka.smarthome.logger.pvstats.model.config

import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType

// todo: device type should no longer be used after old pvstats module migration
enum class SourceType(val deviceType: DeviceType) {
    SMA(DeviceType.SMA),
    SOFAR_WIFI(DeviceType.SOFAR),
    SOFAR_MODBUS(DeviceType.SOFAR),
    AFORE_T6(DeviceType.AFORE)
}
