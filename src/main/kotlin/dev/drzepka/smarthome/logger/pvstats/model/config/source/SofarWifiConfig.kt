package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType

class SofarWifiConfig(name: String, loader: ConfigurationLoader) : SourceConfig(SourceType.SOFAR_WIFI, name, loader) {
    val url: String = loadProperty("url")
    val sn: Long? = loadOptionalProperty("sn")
}