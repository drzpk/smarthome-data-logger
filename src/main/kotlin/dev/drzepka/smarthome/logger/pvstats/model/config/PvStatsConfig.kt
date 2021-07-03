package dev.drzepka.smarthome.logger.pvstats.model.config

import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import java.net.URI

class PvStatsConfig private constructor(
        val url: URI,
        val timeout: Int
) {

    companion object {
        fun loadFromProperties(loader: ConfigurationLoader): PvStatsConfig {
            return PvStatsConfig(
                    URI.create(loader.getValue("pvstats.url", true)!!),
                    loader.getValue("pvstats.timeout", true)!!.toInt()
            )
        }
    }
}