package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType
import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader

class SMAConfig(name: String, loader: ConfigurationLoader) : SourceConfig(SourceType.SMA, name, loader) {
    val url: String = loadProperty("url")
}