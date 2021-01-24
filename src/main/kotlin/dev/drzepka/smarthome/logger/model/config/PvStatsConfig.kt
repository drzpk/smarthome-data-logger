package dev.drzepka.smarthome.logger.model.config

import dev.drzepka.smarthome.logger.util.PropertiesLoader
import java.net.URI

class PvStatsConfig private constructor(
        val url: URI,
        val timeout: Int
) {

    companion object {
        fun loadFromProperties(loader: PropertiesLoader): PvStatsConfig {
            return PvStatsConfig(
                    URI.create(loader.getValue("pvstats.url", true)!!),
                    loader.getValue("pvstats.timeout", true)!!.toInt()
            )
        }
    }
}