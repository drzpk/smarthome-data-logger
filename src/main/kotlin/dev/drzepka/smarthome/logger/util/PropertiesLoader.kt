package dev.drzepka.smarthome.logger.util

import java.io.File
import java.io.FileInputStream
import java.util.*

class PropertiesLoader(properties: Properties? = null) {

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
            javaClass.classLoader.getResourceAsStream("default.properties")!!

        properties.load(stream)
        stream.close()

        return properties
    }
}