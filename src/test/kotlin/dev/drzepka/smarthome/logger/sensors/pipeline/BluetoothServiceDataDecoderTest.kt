package dev.drzepka.smarthome.logger.sensors.pipeline

import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.MacAddress
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
internal class BluetoothServiceDataDecoderTest {

    private val deviceManager = mock<DeviceManager>()

    @Test
    fun `should decode binary data to measurement - positive temperature`() {
        val mac = MacAddress("ab:cd:ef")
        val data = BluetoothServiceData(
            mac,
            "a3 4e 0c 38 c1 a4 8c 0b 62 18 d9 0b 5c fa 04"
        )

        whenever(deviceManager.getDeviceId(eq(mac))).thenReturn(912)

        val measurements = BluetoothServiceDataDecoder(deviceManager).decode(data)
        then(measurements).hasSize(1)

        val measurement = measurements.first()
        then(measurement.temperature).isEqualTo(BigDecimal("29.56"))
        then(measurement.humidity).isEqualTo(BigDecimal("62.42"))
        then(measurement.batteryVoltage).isEqualTo(BigDecimal("3.033"))
        then(measurement.batteryLevel).isEqualTo(92)
        then(measurement.deviceId).isEqualTo(912)
    }

    @Test
    fun `should decode binary data to measurement - negative temperature`() {
        val mac = MacAddress("ab:cd:ef")
        val data = BluetoothServiceData(
            mac,
            "a3 4e 0c 38 c1 a4 4a f8 62 18 d9 0b 5c fa 04"
        )

        whenever(deviceManager.getDeviceId(eq(mac))).thenReturn(111)

        val measurements = BluetoothServiceDataDecoder(deviceManager).decode(data)
        then(measurements).hasSize(1)

        val measurement = measurements.first()
        then(measurement.temperature).isEqualTo(BigDecimal("-19.74"))
    }

    @Test
    fun `should not decode data when there's no device id for given mac`() {
        val data = BluetoothServiceData(
            MacAddress("ab:cd:ef"),
            "a3 4e 0c 38 c1 a4 8c 0b 62 18 d9 0b 5c fa 04"
        )

        whenever(deviceManager.getDeviceId(any())).thenReturn(null)

        val measurements = BluetoothServiceDataDecoder(deviceManager).decode(data)
        then(measurements).isEmpty()
    }
}
