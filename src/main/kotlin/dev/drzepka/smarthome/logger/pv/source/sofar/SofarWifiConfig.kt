package dev.drzepka.smarthome.logger.pv.source.sofar

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource

class SofarWifiConfig(val name: String, source: ConfigPropertySource) {
    val host: String = source.getString("host")
    val port: Int = source.getInt("port", 8899)
    val sn: Long = source.getLong("sn")
    val metricsInterval: Int = source.getInt("metricsInterval", 60)
}
