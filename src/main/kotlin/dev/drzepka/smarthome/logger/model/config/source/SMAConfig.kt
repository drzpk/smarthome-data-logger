package dev.drzepka.smarthome.logger.model.config.source

import dev.drzepka.smarthome.logger.model.config.SourceType
import dev.drzepka.smarthome.logger.util.PropertiesLoader

class SMAConfig(name: String, loader: PropertiesLoader) : SourceConfig(SourceType.SMA, name, loader) {
    val url: String = loadProperty("url")
}