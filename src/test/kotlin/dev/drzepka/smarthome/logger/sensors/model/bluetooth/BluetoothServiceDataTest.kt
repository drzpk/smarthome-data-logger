package dev.drzepka.smarthome.logger.sensors.model.bluetooth

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

class BluetoothServiceDataTest {

    @Test
    fun `should convert hex string to binary`() {
        val data = BluetoothServiceData(MacAddress(""), "df aa f8 12")
        then(data.data[0]).isEqualTo(223.toByte())
        then(data.data[1]).isEqualTo(170.toByte())
        then(data.data[2]).isEqualTo(248.toByte())
        then(data.data[3]).isEqualTo(18.toByte())
    }
}