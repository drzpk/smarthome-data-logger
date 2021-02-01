package dev.drzepka.smarthome.logger.model.config.source

import dev.drzepka.smarthome.logger.model.config.SourceType
import dev.drzepka.smarthome.logger.util.PropertiesLoader

class SofarModbusConfig(name: String, loader: PropertiesLoader) : SourceConfig(SourceType.SOFAR_MODBUS, name, loader) {
    val device: String = loadProperty("devpath")
    val slaveId: Int? = loadProperty("slaveId")
}