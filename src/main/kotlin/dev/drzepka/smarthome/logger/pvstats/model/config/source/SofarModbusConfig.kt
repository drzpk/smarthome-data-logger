package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader

class SofarModbusConfig(name: String, loader: ConfigurationLoader) : SourceConfig(SourceType.SOFAR_MODBUS, name, loader) {
    val device: String = loadProperty("devpath")
    val slaveId: Int? = loadProperty("slaveId")
}