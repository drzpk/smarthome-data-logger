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
                    URI.create(loader.getString("pvstats.url", true)!!),
                    loader.getInt("pvstats.timeout")!!
            )
        }
    }
}