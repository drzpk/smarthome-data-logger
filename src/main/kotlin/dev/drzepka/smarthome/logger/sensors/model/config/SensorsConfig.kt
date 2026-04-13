package dev.drzepka.smarthome.logger.sensors.model.config

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.config.SensorsProperties

class SensorsConfig private constructor(
    val serverUrl: String,
    val loggerId: Int,
    val loggerSecret: String
) {
    companion object {
        fun load(source: ConfigPropertySource): SensorsConfig? {
            if (source.getKeys("sensors").isEmpty())
                return null

            val props = SensorsProperties(source.getChild("sensors"))
            return SensorsConfig(props.serverUrl, props.loggerId, props.loggerSecret)
        }
    }
}
