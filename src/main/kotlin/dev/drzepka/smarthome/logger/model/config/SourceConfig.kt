package dev.drzepka.smarthome.logger.model.config

import dev.drzepka.smarthome.logger.util.PropertiesLoader
import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType

class SourceConfig internal constructor(
        val name: String,
        val type: DeviceType,
        val url: String,
        val user: String,
        val password: String,
        val sn: Int?,
        val timeout: Int,
        val metricsInterval: Int?,
        val measurementInterval: Int?
) {

    companion object {
        fun loadFromProperties(sourceName: String, loader: PropertiesLoader): SourceConfig {
            return SourceConfig(
                    sourceName,
                    DeviceType.valueOf(loader.getString("source.$sourceName.type", true)!!),
                    loader.getString("source.$sourceName.url", true)!!,
                    loader.getString("source.$sourceName.user", true)!!,
                    loader.getString("source.$sourceName.password", true)!!,
                    loader.getInt("source.$sourceName.sn", false),
                    loader.getInt("source.$sourceName.timeout", true)!!,
                    loader.getInt("source.$sourceName.metrics_interval", false),
                    loader.getInt("source.$sourceName.measurement_interval", false)
            )
        }

        fun getAvailableNames(loader: PropertiesLoader): List<String> {
            val regex = Regex("^source\\.([a-zA-Z_-]+)\\..*$")

            val names = ArrayList<String>()
            loader.properties.keys.forEach {
                val result = regex.matchEntire(it as String)
                if (result != null)
                    names.add(result.groupValues[1].toLowerCase())
            }

            return names.distinct()
        }
    }
}