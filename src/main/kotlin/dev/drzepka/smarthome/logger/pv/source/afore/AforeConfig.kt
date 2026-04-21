package dev.drzepka.smarthome.logger.pv.source.afore

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType
import dev.drzepka.smarthome.logger.pvstats.model.config.source.SourceConfig

class AforeConfig(name: String, source: ConfigPropertySource) : SourceConfig(SourceType.AFORE_T6, name, source) {
    val url: String = loadProperty("url")
    val sn: Long? = loadOptionalProperty("sn")
}
