package dev.drzepka.smarthome.logger.model.config.source

import dev.drzepka.smarthome.logger.model.config.SourceType
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