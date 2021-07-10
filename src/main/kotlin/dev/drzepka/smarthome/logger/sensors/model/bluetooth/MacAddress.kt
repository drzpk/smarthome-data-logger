package dev.drzepka.smarthome.logger.sensors.model.bluetooth

class MacAddress(val value: String) {

    override fun equals(other: Any?): Boolean {
        if (other !is MacAddress)
            return false

        return value.toLowerCase() == other.value.toLowerCase()
    }

    override fun hashCode(): Int {
        return value.toLowerCase().hashCode()
    }
}