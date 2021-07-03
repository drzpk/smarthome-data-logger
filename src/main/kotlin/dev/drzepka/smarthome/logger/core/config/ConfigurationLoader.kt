package dev.drzepka.smarthome.logger.core.config

import java.io.File
import java.io.FileInputStream
import java.lang.IllegalStateException
import java.util.*

class ConfigurationLoader(properties: Properties? = null) {

    val properties: Properties

    init {
        this.properties = properties ?: loadProperties()
    }

    fun getValue(name: String, required: Boolean): String? {
        val value = properties.getProperty(name)
        if (value == null && required)
            throw IllegalArgumentException("Property $name is required but wasn't found")
        return value
    }

    private fun loadProperties(): Properties {
        val properties = Properties()
        val file = File("config.properties")
        val stream = if (file.isFile)
            FileInputStream(file)
        else
            throw IllegalStateException("config.properties file wasn't found")

        properties.load(stream)
        stream.close()

        return properties
    }
}