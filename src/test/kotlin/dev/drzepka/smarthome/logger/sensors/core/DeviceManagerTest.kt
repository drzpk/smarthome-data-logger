package dev.drzepka.smarthome.logger.sensors.core

import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.Device
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThatIllegalStateException
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class DeviceManagerTest {

    private val executor = mock<SensorsRequestExecutor>()
    private val context = mock<PipelineContext>(defaultAnswer = ReturnsDeepStubs())

    private val stringCaptor = argumentCaptor<String>()
    private val taskCaptor = argumentCaptor<suspend () -> Unit>()

    @Test
    fun `should not start device manager if it wasn't initialized`() {
        val manager = DeviceManager(executor)

        assertThatIllegalStateException()
            .isThrownBy { manager.start(context) }
            .withMessage("Data source wasn't initialized")
    }

    @Test
    fun `should refresh devices when initializing`() = runBlockingTest {
        val device = Device().apply {
            id = 192
            mac = "aa:bb:cc"
        }
        whenever(executor.getDevices()).thenReturn(listOf(device))

        val manager = DeviceManager(executor)
        manager.initialize()

        then(manager.getDeviceId(MacAddress("aa:bb:cc"))).isEqualTo(192)
        then(manager.getDeviceId(MacAddress("x:y:z"))).isNull()
    }

    @Test
    fun `should refresh devices during initialization until a successful response is returned`() = runBlockingTest {
        val device = Device().apply {
            id = 1234
            mac = "aa:bb:cc"
        }

        val exception = RuntimeException("no network")
        whenever(executor.getDevices())
            .thenThrow(exception, exception, exception)
            .thenReturn(listOf(device))

        val manager = DeviceManager(executor)
        manager.initialize()

        then(manager.getDeviceId(MacAddress("aa:bb:cc"))).isEqualTo(1234)
    }

    @Test
    fun `should schedule device refresh when starting`() = runBlockingTest {
        whenever(executor.getDevices()).thenReturn(emptyList())

        val manager = DeviceManager(executor)
        manager.initialize()
        manager.start(context)

        verify(context.scheduler).schedule(any(), any(), any())
    }

    @Test
    fun `should refresh devices with scheduler`() = runBlockingTest {
        val device = Device().apply {
            id = 1234
            mac = "aa:bb:cc"
        }

        whenever(executor.getDevices()).thenReturn(emptyList(), listOf(device))

        val manager = DeviceManager(executor)
        manager.initialize()
        manager.start(context)

        then(manager.getDeviceId(MacAddress("aa:bb:cc"))).isNull()

        verify(context.scheduler).schedule(any(), any(), taskCaptor.capture())
        val task = taskCaptor.firstValue

        task.invoke()
        then(manager.getDeviceId(MacAddress("aa:bb:cc"))).isEqualTo(1234)
    }

    @Test
    fun `should cancel task when stopping the manager`() = runBlockingTest {
        whenever(executor.getDevices()).thenReturn(emptyList())

        val manager = DeviceManager(executor)
        manager.initialize()
        manager.start(context)
        manager.stop(context)

        verify(context.scheduler).schedule(stringCaptor.capture(), any(), any())

        val name = stringCaptor.firstValue
        verify(context.scheduler).cancel(eq(name))
    }
}
