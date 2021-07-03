package dev.drzepka.smarthome.logger.pvstats.model.config

import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader

class MainConfig private constructor(
        val logDirectory: String
) {

    companion object {
        fun loadFromProperties(loader: ConfigurationLoader): MainConfig {
            return MainConfig(
                    loader.getValue("log_directory", true)!!
            )
        }
    }
}