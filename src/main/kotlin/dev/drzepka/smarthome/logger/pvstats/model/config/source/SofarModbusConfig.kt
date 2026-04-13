package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType

class SofarModbusConfig(name: String, source: ConfigPropertySource) : SourceConfig(SourceType.SOFAR_MODBUS, name, source) {
    val device: String = loadProperty("devpath")
    val slaveId: Int? = loadProperty("slaveId")
}