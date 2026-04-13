package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType

class SofarWifiConfig(name: String, source: ConfigPropertySource) : SourceConfig(SourceType.SOFAR_WIFI, name, source) {
    val url: String = loadProperty("url")
    val sn: Long? = loadOptionalProperty("sn")
}
