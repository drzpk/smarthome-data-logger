package dev.drzepka.smarthome.logger.core.config

import java.time.Duration

class ServerDataSenderProperties(source: ConfigPropertySource) {
    val sendInterval: Duration = Duration.parse(source.getString("server.sender.sendInterval", "PT30S"))
    val maxBatchSize: Int = source.getInt("server.sender.maxBatchSize", 30)
    val maxAge: Duration = Duration.parse(source.getString("server.sender.maxAge", "PT48H"))
    val maxSize: Int = source.getInt("server.sender.maxSize", 15000)
    val errorThreshold: Int = source.getInt("server.sender.errorThreshold", 3)
    val throttleDelay: Duration = Duration.parse(source.getString("server.sender.throttleDelay", "PT2M"))
    val throttleBackoffFactor: Double =
        source.getString("server.sender.throttleBackoffFactor", "2.0").toDouble()
    val maxThrottleDelay: Duration = Duration.parse(source.getString("server.sender.maxThrottleDelay", "PT10M"))

    val throttleSkipCount: Int = throttleDelay.dividedBy(sendInterval).toInt()
    val maxThrottleSkipCount: Int = maxThrottleDelay.dividedBy(sendInterval).toInt()
}
