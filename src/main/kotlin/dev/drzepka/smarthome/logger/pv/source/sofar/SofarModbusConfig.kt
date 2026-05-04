package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource

class SofarModbusConfig(val name: String, source: ConfigPropertySource) {
    val device: String = source.getString("device")
    val slaveId: Int = source.getInt("slaveId", 1)
    val metricsInterval: Int = source.getInt("metricsInterval", 60)
}
