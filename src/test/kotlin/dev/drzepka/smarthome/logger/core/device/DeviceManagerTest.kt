package dev.drzepka.smarthome.logger.core.device

import dev.drzepka.smarthome.logger.core.model.Device
import dev.drzepka.smarthome.logger.core.model.MacAddress
import dev.drzepka.smarthome.logger.core.scheduler.TaskScheduler
import dev.drzepka.smarthome.logger.core.transport.ServerRequestExecutor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.BDDAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class DeviceManagerTest {

    private val executor = mock<ServerRequestExecutor>()
    private val scheduler = mock<TaskScheduler>()

    private val stringCaptor = argumentCaptor<String>()
    private val taskCaptor = argumentCaptor<suspend () -> Unit>()

    @Test
    fun `should not start device manager if it wasn't initialized`() {
        val manager = OnlineDeviceManager(executor, scheduler)

        Assertions.assertThatIllegalStateException()
            .isThrownBy { manager.start() }
            .withMessage("Data source wasn't initialized")
    }

    @Test
    fun `should refresh devices when initializing`() = runTest {
        val device = Device(id = 192, mac = "aa:bb:cc")
        whenever(executor.getDevices()).thenReturn(listOf(device))

        val manager = OnlineDeviceManager(executor, scheduler)
        manager.initialize()

        BDDAssertions.then(manager.getDeviceId(MacAddress("aa:bb:cc"))).isEqualTo(192)
        BDDAssertions.then(manager.getDeviceId(MacAddress("x:y:z"))).isNull()
    }

    @Test
    fun `should refresh devices during initialization until a successful response is returned`() = runTest {
        val device = Device(id = 1234, mac = "aa:bb:cc")

        val exception = RuntimeException("no network")
        whenever(executor.getDevices())
            .thenThrow(exception, exception, exception)
            .thenReturn(listOf(device))

        val manager = OnlineDeviceManager(executor, scheduler)
        manager.initialize()

        BDDAssertions.then(manager.getDeviceId(MacAddress("aa:bb:cc"))).isEqualTo(1234)
    }

    @Test
    fun `should schedule device refresh when starting`() = runTest {
        whenever(executor.getDevices()).thenReturn(emptyList())

        val manager = OnlineDeviceManager(executor, scheduler)
        manager.initialize()
        manager.start()

        verify(scheduler).schedule(any(), any(), any())
    }

    @Test
    fun `should refresh devices with scheduler`() = runTest {
        val device = Device(id = 1234, mac = "aa:bb:cc")

        whenever(executor.getDevices()).thenReturn(emptyList(), listOf(device))

        val manager = OnlineDeviceManager(executor, scheduler)
        manager.initialize()
        manager.start()

        BDDAssertions.then(manager.getDeviceId(MacAddress("aa:bb:cc"))).isNull()

        verify(scheduler).schedule(any(), any(), taskCaptor.capture())
        val task = taskCaptor.firstValue

        task.invoke()
        BDDAssertions.then(manager.getDeviceId(MacAddress("aa:bb:cc"))).isEqualTo(1234)
    }

    @Test
    fun `should cancel task when stopping the manager`() = runTest {
        whenever(executor.getDevices()).thenReturn(emptyList())

        val manager = OnlineDeviceManager(executor, scheduler)
        manager.initialize()
        manager.start()
        manager.stop()

        verify(scheduler).schedule(stringCaptor.capture(), any(), any())

        val name = stringCaptor.firstValue
        verify(scheduler).cancel(eq(name))
    }
}