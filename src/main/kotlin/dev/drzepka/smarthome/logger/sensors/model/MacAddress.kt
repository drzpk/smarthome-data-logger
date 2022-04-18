package dev.drzepka.smarthome.logger.sensors.model

class MacAddress(val value: String) {

    override fun equals(other: Any?): Boolean {
        if (other !is MacAddress)
            return false

        return value.equals(other.value, ignoreCase = true)
    }

    override fun hashCode(): Int {
        return value.toLowerCase().hashCode()
    }

    override fun toString(): String = value
}
