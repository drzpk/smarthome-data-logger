package dev.drzepka.smarthome.logger.core.config

import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.api.BDDAssertions.thenThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PropertiesConfigPropertySourceTest {

    @Nested
    inner class GetString {
        @Test
        fun `should return value`() {
            then(source("key=value").getString("key")).isEqualTo("value")
        }

        @Test
        fun `should return default when key is absent`() {
            then(source("").getString("missing", "default")).isEqualTo("default")
        }

        @Test
        fun `should throw PropertyNotFoundException when required key is absent`() {
            thenThrownBy { source("").getString("missing") }
                .isInstanceOf(PropertyNotFoundException::class.java)
                .hasMessageContaining("missing")
        }

        @Test
        fun `getOptionalString should return value or null when absent`() {
            then(source("key=value").getOptionalString("key")).isEqualTo("value")
            then(source("").getOptionalString("missing")).isNull()
        }
    }

    @Nested
    inner class GetInt {
        @Test
        fun `should return value`() {
            then(source("port=8080").getInt("port")).isEqualTo(8080)
        }

        @Test
        fun `should return default when key is absent`() {
            then(source("").getInt("port", 9000)).isEqualTo(9000)
        }

        @Test
        fun `should throw PropertyNotFoundException when required key is absent`() {
            thenThrownBy { source("").getInt("port") }
                .isInstanceOf(PropertyNotFoundException::class.java)
                .hasMessageContaining("port")
        }

        @Test
        fun `should throw InvalidPropertyValueException when value is not a number`() {
            thenThrownBy { source("port=abc").getInt("port") }
                .isInstanceOf(InvalidPropertyValueException::class.java)
                .hasMessageContaining("port")
                .hasMessageContaining("abc")
        }

        @Test
        fun `getOptionalInt should return value, null when absent, or throw on invalid`() {
            then(source("port=8080").getOptionalInt("port")).isEqualTo(8080)
            then(source("").getOptionalInt("port")).isNull()
            thenThrownBy { source("port=abc").getOptionalInt("port") }
                .isInstanceOf(InvalidPropertyValueException::class.java)
        }
    }

    @Nested
    inner class GetLong {
        @Test
        fun `should return value`() {
            then(source("sn=1234567890123").getLong("sn")).isEqualTo(1234567890123L)
        }

        @Test
        fun `should return default when key is absent`() {
            then(source("").getLong("sn", 0L)).isEqualTo(0L)
        }

        @Test
        fun `should throw PropertyNotFoundException when required key is absent`() {
            thenThrownBy { source("").getLong("sn") }
                .isInstanceOf(PropertyNotFoundException::class.java)
                .hasMessageContaining("sn")
        }

        @Test
        fun `should throw InvalidPropertyValueException when value is not a number`() {
            thenThrownBy { source("sn=abc").getLong("sn") }
                .isInstanceOf(InvalidPropertyValueException::class.java)
                .hasMessageContaining("sn")
        }

        @Test
        fun `getOptionalLong should return value, null when absent, or throw on invalid`() {
            then(source("sn=1234567890123").getOptionalLong("sn")).isEqualTo(1234567890123L)
            then(source("").getOptionalLong("sn")).isNull()
            thenThrownBy { source("sn=abc").getOptionalLong("sn") }
                .isInstanceOf(InvalidPropertyValueException::class.java)
        }
    }

    @Nested
    inner class GetBoolean {
        @Test
        fun `should return true and false`() {
            then(source("enabled=true").getBoolean("enabled")).isTrue()
            then(source("enabled=false").getBoolean("enabled")).isFalse()
        }

        @Test
        fun `should return default when key is absent`() {
            then(source("").getBoolean("enabled", false)).isFalse()
        }

        @Test
        fun `should throw PropertyNotFoundException when required key is absent`() {
            thenThrownBy { source("").getBoolean("enabled") }
                .isInstanceOf(PropertyNotFoundException::class.java)
                .hasMessageContaining("enabled")
        }

        @Test
        fun `should throw InvalidPropertyValueException when value is not a boolean`() {
            thenThrownBy { source("enabled=yes").getBoolean("enabled") }
                .isInstanceOf(InvalidPropertyValueException::class.java)
                .hasMessageContaining("enabled")
        }

        @Test
        fun `getOptionalBoolean should return value, null when absent, or throw on invalid`() {
            then(source("enabled=true").getOptionalBoolean("enabled")).isTrue()
            then(source("").getOptionalBoolean("enabled")).isNull()
            thenThrownBy { source("enabled=yes").getOptionalBoolean("enabled") }
                .isInstanceOf(InvalidPropertyValueException::class.java)
        }
    }

    @Nested
    inner class GetEnum {
        @Test
        fun `should return value`() {
            then(source("type=WIFI").getEnum<TestEnum>("type")).isEqualTo(TestEnum.WIFI)
        }

        @Test
        fun `should match value case-insensitively`() {
            then(source("type=wifi").getEnum<TestEnum>("type")).isEqualTo(TestEnum.WIFI)
        }

        @Test
        fun `should throw PropertyNotFoundException when required key is absent`() {
            thenThrownBy { source("").getEnum<TestEnum>("type") }
                .isInstanceOf(PropertyNotFoundException::class.java)
                .hasMessageContaining("type")
        }

        @Test
        fun `should throw InvalidPropertyValueException when value is not a valid enum constant`() {
            thenThrownBy { source("type=INVALID").getEnum<TestEnum>("type") }
                .isInstanceOf(InvalidPropertyValueException::class.java)
                .hasMessageContaining("INVALID")
        }
    }

    @Nested
    inner class GetKeys {
        @Test
        fun `should return immediate child keys`() {
            val s = source(
                """
                pv.source.a.host=192.168.1.1
                pv.source.a.port=8080
                pv.source.b.host=192.168.1.2
                other.key=value
                """.trimIndent()
            )
            then(s.getKeys("pv.source")).containsExactlyInAnyOrder("a", "b")
        }

        @Test
        fun `should return empty list when prefix is absent`() {
            then(source("other.key=value").getKeys("pv.source")).isEmpty()
        }
    }

    @Nested
    inner class GetChild {
        @Test
        fun `should set path and resolve keys relative to it`() {
            val s = source("pv.source.afore.host=192.168.1.1\npv.source.afore.port=8080")
            val child = s.getChild("pv.source.afore")
            then(child.path).isEqualTo("pv.source.afore")
            then(child.getString("host")).isEqualTo("192.168.1.1")
            then(child.getInt("port")).isEqualTo(8080)
        }

        @Test
        fun `should not expose keys outside its path`() {
            val s = source("pv.source.afore.host=192.168.1.1\nother.key=value")
            val child = s.getChild("pv.source.afore")
            then(child.getOptionalString("other.key")).isNull()
        }

        @Test
        fun `chained calls should extend path`() {
            val s = source("a.b.c.key=hello")
            val child = s.getChild("a").getChild("b").getChild("c")
            then(child.path).isEqualTo("a.b.c")
            then(child.getString("key")).isEqualTo("hello")
        }
    }

    private fun source(content: String) = PropertiesConfigPropertySource(content)

    @Suppress("unused")
    private enum class TestEnum { WIFI, MODBUS }
}
