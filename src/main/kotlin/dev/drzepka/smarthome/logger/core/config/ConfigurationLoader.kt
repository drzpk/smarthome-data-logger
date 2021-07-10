package dev.drzepka.smarthome.logger.core.config

import dev.drzepka.smarthome.common.util.Logger
import java.io.File
import java.io.FileInputStream
import java.lang.IllegalStateException
import java.util.*

class ConfigurationLoader(properties: Properties? = null) {

    val properties: Properties

    private val log by Logger()

    init {
        this.properties = properties ?: loadProperties()
    }

    fun getInt(name: String, required: Boolean): Int? {
        val value = getValue(name, required)
        return if (required) value!!.toInt() else null
    }

    fun getValue(name: String, required: Boolean): String? {
        val value = properties.getProperty(name)
        if (value == null && required)
            throw IllegalArgumentException("Property $name is required but wasn't found")
        return value
    }

    fun containsKey(name: String): Boolean {
        fun split(input: String): List<String> = input.split(".")

        val splitName = split(name)
        fun matches(input: Collection<String>): Boolean = splitName
            .zip(input)
            .all { it.first == it.second }

        return properties.keys
            .map { split(it as String) }
            .any { matches(it) }
    }

    private fun loadProperties(): Properties {
        val properties = Properties()
        val filename = getPropertiesName()
        val file = File(filename)
        val stream = if (file.isFile)
            FileInputStream(file)
        else
            throw IllegalStateException("$filename file wasn't found")

        properties.load(stream)
        stream.close()

        return properties
    }

    private fun getPropertiesName(): String {
        val overrideName = System.getProperty(PROPERTIES_FILE_NAME_SYSTEM_PROPERTY)
        if (overrideName?.isNotBlank() == true) {
            log.info("Detected custom configuration file: {}", overrideName)
            return overrideName
        }

        return DEFAULT_PROPERTIES_FILE_NAME
    }

    companion object {
        private const val PROPERTIES_FILE_NAME_SYSTEM_PROPERTY = "LOGGER_PROPERTIES"
        private const val DEFAULT_PROPERTIES_FILE_NAME = "config.properties"
    }
}