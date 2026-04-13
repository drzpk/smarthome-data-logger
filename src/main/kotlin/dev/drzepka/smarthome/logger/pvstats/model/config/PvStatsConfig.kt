package dev.drzepka.smarthome.logger.pvstats.model.config

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import java.net.URI

class PvStatsConfig private constructor(
        val url: URI,
        val timeout: Int
) {
    companion object {
        fun load(source: ConfigPropertySource): PvStatsConfig {
            return PvStatsConfig(
                    URI.create(source.getString("pvstats.url")),
                    source.getInt("pvstats.timeout")
            )
        }
    }
}
