package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType

class AforeConfig(name: String, source: ConfigPropertySource) : SourceConfig(SourceType.AFORE_T6, name, source) {
    val url: String = loadProperty("url")
    val sn: Long? = loadOptionalProperty("sn")
}
