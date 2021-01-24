package dev.drzepka.smarthome.logger.model.config.source

import dev.drzepka.smarthome.common.pvstats.model.vendor.DeviceType
import dev.drzepka.smarthome.logger.util.PropertiesLoader

class SofarConfig(name: String, loader: PropertiesLoader) : SourceConfig(DeviceType.SOFAR, name, loader) {
    val url: String = loadProperty("url")
    val sn: Int? = loadOptionalProperty("sn")
}