package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType

object SourceConfigFactory {

    fun getAvailableNames(source: ConfigPropertySource): List<String> {
        return source.getKeys("pvstats.source")
    }

    fun createSourceConfig(sourceName: String, source: ConfigPropertySource): SourceConfig {
        val typeString = source.getString("pvstats.source.$sourceName.type")

        val typeValue = try {
            SourceType.valueOf(typeString)
        } catch (e: Exception) {
            throw IllegalStateException("Error while loading source '$sourceName'", e)
        }

        return when (typeValue) {
            SourceType.SMA -> SMAConfig(sourceName, source)
            SourceType.SOFAR_WIFI -> SofarWifiConfig(sourceName, source)
            SourceType.SOFAR_MODBUS -> SofarModbusConfig(sourceName, source)
            SourceType.AFORE_T6 -> AforeConfig(sourceName, source)
        }
    }
}
