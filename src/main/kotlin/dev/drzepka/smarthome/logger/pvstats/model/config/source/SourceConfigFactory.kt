package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType

object SourceConfigFactory {

    fun getAvailableNames(loader: ConfigurationLoader): List<String> {
        val regex = Regex("^pvstats\\.source\\.([a-zA-Z0-9_-]+)\\..*$")

        val names = ArrayList<String>()
        loader.properties.keys.forEach {
            val result = regex.matchEntire(it as String)
            if (result != null)
                names.add(result.groupValues[1].toLowerCase())
        }

        return names.distinct()
    }

    fun createSourceConfig(sourceName: String, loader: ConfigurationLoader): SourceConfig {
        val typeString = loader.getValue("pvstats.source.$sourceName.type", true)!!

        val typeValue = try {
            SourceType.valueOf(typeString)
        } catch (e: Exception) {
            throw IllegalStateException("Error while loading source '$sourceName'", e)
        }

        return when(typeValue) {
            SourceType.SMA -> SMAConfig(sourceName, loader)
            SourceType.SOFAR_WIFI -> SofarWifiConfig(sourceName, loader)
            SourceType.SOFAR_MODBUS -> SofarModbusConfig(sourceName, loader)
        }
    }
}