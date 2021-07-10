package dev.drzepka.smarthome.logger.sensors.converter

import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.MacAddress
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class BluetoothServiceDataToMeasurementConverterTest {
    @Test
    fun `should convert binary data to measurement - positive temperature`() {
        val data = BluetoothServiceData(
            MacAddress("ab:cd:ef"),
            "a3 4e 0c 38 c1 a4 8c 0b 62 18 d9 0b 5c fa 04"
        )
        val request = BluetoothServiceDataToMeasurementConverter.convertCustomFormat(data)

        then(request.temperature).isEqualTo(BigDecimal("29.56"))
        then(request.humidity).isEqualTo(BigDecimal("62.42"))
        then(request.batteryVoltage).isEqualTo(BigDecimal("3.033"))
        then(request.batteryLevel).isEqualTo(92)
    }

    @Test
    fun `should convert binary data to measurement - negative temperature`() {
        val data = BluetoothServiceData(
            MacAddress("ab:cd:ef"),
            "a3 4e 0c 38 c1 a4 4a f8 62 18 d9 0b 5c fa 04"
        )
        val request = BluetoothServiceDataToMeasurementConverter.convertCustomFormat(data)

        then(request.temperature).isEqualTo(BigDecimal("-19.74"))
    }
}