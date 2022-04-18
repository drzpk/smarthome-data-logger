package dev.drzepka.smarthome.logger.sensors.pipeline.decoder

import dev.drzepka.smarthome.logger.sensors.model.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class BluetoothServiceDataDecoderTest {

    @Test
    fun `should decode binary data to measurement - positive temperature`() {
        val mac = MacAddress("ab:cd:ef")
        val data = BluetoothServiceData(
            mac,
            "a3 4e 0c 38 c1 a4 8c 0b 62 18 d9 0b 5c fa 04"
        )

        val measurements = BluetoothServiceDataDecoder.decode(data)
        then(measurements).hasSize(1)

        val measurement = measurements.first().measurement
        then(measurement.temperature).isEqualTo(BigDecimal("29.56"))
        then(measurement.humidity).isEqualTo(BigDecimal("62.42"))
        then(measurement.batteryVoltage).isEqualTo(BigDecimal("3.033"))
        then(measurement.batteryLevel).isEqualTo(92)
    }

    @Test
    fun `should decode binary data to measurement - negative temperature`() {
        val mac = MacAddress("ab:cd:ef")
        val data = BluetoothServiceData(
            mac,
            "a3 4e 0c 38 c1 a4 4a f8 62 18 d9 0b 5c fa 04"
        )

        val measurements = BluetoothServiceDataDecoder.decode(data)
        then(measurements).hasSize(1)

        val measurement = measurements.first().measurement
        then(measurement.temperature).isEqualTo(BigDecimal("-19.74"))
    }
}
