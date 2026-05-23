package dev.drzepka.smarthome.logger.pv.source.sma

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import java.net.URI

class SmaConfig(val name: String, source: ConfigPropertySource) {
    val url: String = source.getString("url")
    val timeout: Int = source.getInt("timeout", 10)
    val interval: Int = source.getInt("metricsInterval", 30)

    val host: String get() = URI.create(url).host
}
