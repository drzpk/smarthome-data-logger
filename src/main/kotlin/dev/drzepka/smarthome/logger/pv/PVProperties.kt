package dev.drzepka.smarthome.logger.pv

import dev.drzepka.smarthome.logger.core.config.ConfigPropertySource
import dev.drzepka.smarthome.logger.core.config.getEnum
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType

class PVProperties(source: ConfigPropertySource) {
    val enabled = source.getBoolean("pv.enabled", false)
    val sources: Map<String, PVSource> = source.getKeys("pv.source").associateWith {
        PVSource(source.getChild("pv.source.$it"))
    }
}

class PVSource(source: ConfigPropertySource) {
    val type = source.getEnum<SourceType>("type")
    val host = source.getString("host")
    val port = source.getInt("port", 8899)
    val sn = source.getLong("sn")
}