package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType

class SMAConfig(name: String, source: ConfigPropertySource) : SourceConfig(SourceType.SMA, name, source) {
    val url: String = loadProperty("url")
}