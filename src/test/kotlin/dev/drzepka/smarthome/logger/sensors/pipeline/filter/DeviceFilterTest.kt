package dev.drzepka.smarthome.logger.sensors.pipeline.filter

import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.model.LocalMeasurement
import dev.drzepka.smarthome.logger.sensors.model.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.Measurement
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
internal class DeviceFilterTest {

    private val deviceManager = mock<DeviceManager>()
    private val context = mock<PipelineContext>()

     @Test
    fun `should forward start event to device manager`() {
        DeviceFilter(deviceManager).start(context)
        verify(deviceManager).start(same(context))
    }

    @Test
    fun `should forward stop event to device manager`() {
        DeviceFilter(deviceManager).stop(context)
        verify(deviceManager).stop(same(context))
    }

    @Test
    fun `should pass measurement with known mac`() {
        val mac = MacAddress("mac")
        val deviceId = 234
        whenever(deviceManager.getDeviceId(eq(mac))).thenReturn(deviceId)

        val data = LocalMeasurement(mac, Measurement())
        val filter = DeviceFilter(deviceManager)

        val output = filter.filter(data)

        then(output).isSameAs(data)
        then(output!!.measurement.deviceId).isEqualTo(deviceId)
    }

    @Test
    fun `should reject measurement with unknown mac`() {
        whenever(deviceManager.getDeviceId(any())).thenReturn(null)

        val data = LocalMeasurement(MacAddress("test"), Measurement())
        val filter = DeviceFilter(deviceManager)

        val output = filter.filter(data)

        then(output).isNull()
    }
}
