package dev.drzepka.smarthome.logger.model.config.source

import dev.drzepka.smarthome.logger.model.config.SourceType
import dev.drzepka.smarthome.logger.util.PropertiesLoader
import kotlin.reflect.KClass

abstract class SourceConfig internal constructor(
        val type: SourceType,
        val name: String,
        protected val loader: PropertiesLoader
) {
    val user: String = loadProperty("user")
    val password: String = loadProperty("password")
    val timeout: Int = loadProperty("timeout")
    val metricsInterval: Int? = loadOptionalProperty("metrics_interval")
    val measurementInterval: Int? = loadOptionalProperty("measurement_interval")

    protected inline fun <reified T : Any> loadProperty(configPath: String): T {
        val fullPath = "source.$name.$configPath"
        val value = loader.getValue(fullPath, true)!!
        return convertToRequiredType(value, T::class)
    }

    protected inline fun <reified T : Any> loadOptionalProperty(configPath: String): T? {
        val fullPath = "source.$name.$configPath"
        val value = loader.getValue(fullPath, false)
        return value?.let { convertToRequiredType(it, T::class) }
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    protected fun <T : Any> convertToRequiredType(input: String, targetType: KClass<T>): T {
        return when (targetType) {
            Int::class -> Integer.parseInt(input)
            String::class -> input
            else -> throw IllegalArgumentException("No converter found for input string '$input' to target type ${targetType.java.simpleName}")
        } as T
    }
}