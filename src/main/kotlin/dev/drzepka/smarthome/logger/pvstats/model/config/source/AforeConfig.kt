package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType

class AforeConfig(name: String, loader: ConfigurationLoader) : SourceConfig(SourceType.AFORE_T6, name, loader) {
    val url: String = loadProperty("url")
    val sn: Long? = loadOptionalProperty("sn")
}
