package dev.drzepka.smarthome.logger.core.config

import java.io.IOException
import java.io.StringReader
import java.util.*

class PropertiesConfigPropertySource private constructor(
    override val path: String,
    private val properties: Properties
) : ConfigPropertySource {

    constructor(content: String) : this("", parseProperties(content))

    override fun getOptionalString(key: String): String? = properties.getProperty(fullKey(key))

    override fun getKeys(key: String): List<String> {
        val prefix = fullKey(key) + "."
        return properties.keys
            .map { it as String }
            .filter { it.startsWith(prefix) }
            .map { it.removePrefix(prefix).substringBefore('.') }
            .distinct()
    }

    override fun getChild(key: String): ConfigPropertySource =
        PropertiesConfigPropertySource(fullKey(key), properties)

    private fun fullKey(key: String): String = if (path.isEmpty()) key else "$path.$key"

    companion object {
        private fun parseProperties(content: String): Properties {
            return try {
                Properties().also { it.load(StringReader(content)) }
            } catch (e: IOException) {
                throw IllegalArgumentException("Failed to parse properties content: ${e.message}", e)
            }
        }
    }
}
