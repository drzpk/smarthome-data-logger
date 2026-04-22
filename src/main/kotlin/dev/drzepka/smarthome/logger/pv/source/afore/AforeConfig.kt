package dev.drzepka.smarthome.logger.pv.source.afore

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource

class AforeConfig(val name: String, source: ConfigPropertySource) {
    val host: String = source.getString("host")
    val port: Int = source.getInt("port", 8899)
    val sn: Long = source.getLong("sn")
    val slaveAddress: Int = source.getInt("slaveAddress", 1)
    val measurementInterval: Int? = source.getOptionalInt("measurementInterval")
}
