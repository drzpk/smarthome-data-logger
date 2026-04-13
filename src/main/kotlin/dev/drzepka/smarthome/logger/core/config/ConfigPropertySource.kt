package dev.drzepka.smarthome.logger.core.config

interface ConfigPropertySource {
    val path: String

    fun getString(key: String, default: String? = null): String {
        return getOptionalString(key)
            ?: default
            ?: throw PropertyNotFoundException(key, path)
    }

    fun getInt(key: String, default: Int? = null): Int {
        val raw = getOptionalString(key)
            ?: return default ?: throw PropertyNotFoundException(key, path)
        return raw.toIntOrNull()
            ?: throw InvalidPropertyValueException(key, raw, "integer", path)
    }

    fun getLong(key: String, default: Long? = null): Long {
        val raw = getOptionalString(key)
            ?: return default ?: throw PropertyNotFoundException(key, path)
        return raw.toLongOrNull()
            ?: throw InvalidPropertyValueException(key, raw, "long", path)
    }

    fun getBoolean(key: String, default: Boolean? = null): Boolean {
        val raw = getOptionalString(key)
            ?: return default ?: throw PropertyNotFoundException(key, path)
        return raw.toBooleanStrictOrNull()
            ?: throw InvalidPropertyValueException(key, raw, "boolean", path)
    }

    fun getOptionalInt(key: String): Int? = getOptionalString(key)?.let {
        it.toIntOrNull() ?: throw InvalidPropertyValueException(key, it, "integer", path)
    }

    fun getOptionalLong(key: String): Long? = getOptionalString(key)?.let {
        it.toLongOrNull() ?: throw InvalidPropertyValueException(key, it, "long", path)
    }

    fun getOptionalBoolean(key: String): Boolean? = getOptionalString(key)?.let {
        it.toBooleanStrictOrNull() ?: throw InvalidPropertyValueException(key, it, "boolean", path)
    }

    fun <T : Enum<T>> getEnumValue(key: String, enumClass: Class<T>): T {
        val raw = getString(key)
        return enumClass.enumConstants.firstOrNull { it.name.equals(raw, ignoreCase = true) }
            ?: throw InvalidPropertyValueException(key, raw, enumClass.simpleName, path)
    }

    fun getOptionalString(key: String): String?
    fun getKeys(key: String): List<String>
    fun getChild(key: String): ConfigPropertySource
}

inline fun <reified T : Enum<T>> ConfigPropertySource.getEnum(key: String): T =
    getEnumValue(key, T::class.java)

class PropertyNotFoundException(key: String, path: String) :
    Exception("Required property '$key' not found (path: '$path')")

class InvalidPropertyValueException(key: String, value: String, expectedType: String, path: String) :
    Exception("Property '$key' value '$value' is not a valid $expectedType (path: '$path')")
