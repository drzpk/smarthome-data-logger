package dev.drzepka.smarthome.logger.core.config

import java.time.Duration

class ServerDataSenderProperties(source: ConfigPropertySource) {
    val sendInterval: Duration = Duration.parse(source.getString("server.sender.sendInterval", "PT30S"))
    val maxBatchSize: Int = source.getInt("server.sender.maxBatchSize", 30)
    val maxAge: Duration = Duration.parse(source.getString("server.sender.maxAge", "PT48H"))
    val maxSize: Int = source.getInt("server.sender.maxSize", 15000)
}
