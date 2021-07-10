package dev.drzepka.smarthome.logger.core.config

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.util.*

internal class ConfigurationLoaderTest {

    @Test
    fun `should check if properties contain given key`() {
        val properties = Properties()
        properties["key.sub1.sub2"] = "abc"
        properties["key.sub1.sub3"] = "def"
        properties["key_.sub1"] = "ghi"
        properties["key.sub1_.sub2"] = "jkl"

        val loader = ConfigurationLoader(properties)

        then(loader.containsKey("key")).isTrue()
        then(loader.containsKey("key1")).isFalse()
        then(loader.containsKey("ke")).isFalse()

        then(loader.containsKey("key.sub1")).isTrue()
        then(loader.containsKey("key.subxx")).isFalse()
        then(loader.containsKey("key.sub")).isFalse()

        then(loader.containsKey("key.sub1.sub2")).isTrue()
        then(loader.containsKey("key.sub1.subxx")).isFalse()
    }
}