package dev.drzepka.smarthome.logger.model.config.source

import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import dev.drzepka.smarthome.logger.util.PropertiesLoader

object SourceConfigFactory {

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

    fun createSourceConfig(sourceName: String, loader: PropertiesLoader): SourceConfig {
        val typeString = loader.getValue("source.$sourceName.type", true)!!

        return when(DeviceType.valueOf(typeString)) {
            DeviceType.SMA -> SMAConfig(sourceName, loader)
            DeviceType.SOFAR -> SofarConfig(sourceName, loader)
            DeviceType.GENERIC -> throw IllegalArgumentException("Cannot create logger for generic device from source: $sourceName")
        }
    }
}