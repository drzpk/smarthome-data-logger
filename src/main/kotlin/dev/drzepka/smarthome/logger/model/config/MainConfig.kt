package dev.drzepka.smarthome.logger.model.config

import dev.drzepka.smarthome.logger.util.PropertiesLoader

class MainConfig private constructor(
        val logDirectory: String
) {

    companion object {
        fun loadFromProperties(loader: PropertiesLoader): MainConfig {
            return MainConfig(
                    loader.getValue("log_directory", true)!!
            )
        }
    }
}