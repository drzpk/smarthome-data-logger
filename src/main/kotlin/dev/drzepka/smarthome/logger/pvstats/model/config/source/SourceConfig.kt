package dev.drzepka.smarthome.logger.pvstats.model.config.source

import dev.drzepka.smarthome.logger.core.config.ConfigurationLoader
import dev.drzepka.smarthome.logger.pvstats.model.config.SourceType
import kotlin.reflect.KClass

abstract class SourceConfig internal constructor(
        val type: SourceType,
        val name: String,
        protected val loader: ConfigurationLoader
) {
    val user: String = loadProperty("user")
    val password: String = loadProperty("password")
    val timeout: Int = loadProperty("timeout")
    val metricsInterval: Int? = loadOptionalProperty("metrics_interval")
    val measurementInterval: Int? = loadOptionalProperty("measurement_interval")

    protected inline fun <reified T : Any> loadProperty(configPath: String): T {
        val fullPath = "$CONFIG_PREFIX.$name.$configPath"
        val value = loader.getValue(fullPath, true)!!
        return convertToRequiredType(value, T::class)
    }

    protected inline fun <reified T : Any> loadOptionalProperty(configPath: String): T? {
        val fullPath = "$CONFIG_PREFIX.$name.$configPath"
        val value = loader.getValue(fullPath, false)
        return value?.let { convertToRequiredType(it, T::class) }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <T : Any> convertToRequiredType(input: String, targetType: KClass<T>): T {
        return when (targetType) {
            Int::class -> input.toInt()
            Long::class -> input.toLong()
            String::class -> input
            else -> throw IllegalArgumentException("No converter found for input string '$input' to target type ${targetType.java.simpleName}")
        } as T
    }

    companion object {
        const val CONFIG_PREFIX = "pvstats.source"
    }
}