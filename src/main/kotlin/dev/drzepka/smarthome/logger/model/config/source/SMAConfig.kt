package dev.drzepka.smarthome.logger.model.config.source

import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import dev.drzepka.smarthome.logger.util.PropertiesLoader

class SMAConfig(name: String, loader: PropertiesLoader) : SourceConfig(DeviceType.SMA, name, loader) {
    val url: String = loadProperty("url")
}