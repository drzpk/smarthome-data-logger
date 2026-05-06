package dev.drzepka.smarthome.logger.core.config

class SchedulerProperties(source: ConfigPropertySource) {
    val errorThreshold: Int = source.getInt("scheduler.errorThreshold", 3)
    val throttleSkipCount: Int = source.getInt("scheduler.throttleSkipCount", 2)
    val backoffFactor: Double = source.getString("scheduler.backoffFactor", "2.0").toDouble()
    val maxSkipCount: Int = source.getInt("scheduler.maxSkipCount", 10)
}