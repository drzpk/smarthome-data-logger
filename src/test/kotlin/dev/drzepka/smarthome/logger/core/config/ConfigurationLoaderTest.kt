package dev.drzepka.smarthome.logger.core.config

import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Test

internal class ConfigurationLoaderTest {

    @Test
    fun `loadSource should throw when no config file exists`() {
        System.setProperty("LOGGER_PROPERTIES", "non-existent-file.properties")
        try {
            thenThrownBy { ConfigurationLoader().loadSource() }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessageContaining("non-existent-file.properties")
        } finally {
            System.clearProperty("LOGGER_PROPERTIES")
        }
    }
}
