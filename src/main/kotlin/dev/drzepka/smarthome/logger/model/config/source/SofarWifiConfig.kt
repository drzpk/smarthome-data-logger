package dev.drzepka.smarthome.logger.model.config.source

import dev.drzepka.smarthome.logger.model.config.SourceType
import dev.drzepka.smarthome.logger.util.PropertiesLoader

class SofarWifiConfig(name: String, loader: PropertiesLoader) : SourceConfig(SourceType.SOFAR_WIFI, name, loader) {
    val url: String = loadProperty("url")
    val sn: Int? = loadOptionalProperty("sn")
}