package dev.drzepka.smarthome.logger.core.config

import dev.drzepka.smarthome.common.util.Logger
import java.io.File
import java.io.InputStream

class ConfigurationLoader {

    private val log by Logger()

    fun loadSource(): PropertiesConfigPropertySource {
        val stream = tryLoadingInternalProperties() ?: loadExternalProperties()
        return stream.use { PropertiesConfigPropertySource(it.reader().readText()) }
    }

    private fun tryLoadingInternalProperties(): InputStream? {
        return System.getProperty(INTERNAL_PROPERTIES_FILE_SYSTEM_PROPERTY)?.let {
            javaClass.classLoader.getResourceAsStream(it)
        }
    }

    private fun loadExternalProperties(): InputStream {
        val filename = getPropertiesName()
        val file = File(filename)
        if (!file.isFile)
            throw IllegalStateException("$filename file wasn't found")

        return file.inputStream()
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
        private const val INTERNAL_PROPERTIES_FILE_SYSTEM_PROPERTY = "INTERNAL_PROPERTIES"
        private const val DEFAULT_PROPERTIES_FILE_NAME = "config.properties"
    }
}
