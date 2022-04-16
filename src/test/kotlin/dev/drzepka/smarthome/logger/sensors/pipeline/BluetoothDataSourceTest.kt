package dev.drzepka.smarthome.logger.sensors.pipeline

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.sensors.core.DeviceManager
import dev.drzepka.smarthome.logger.sensors.pipeline.datasource.BluetoothDataSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.same
import org.mockito.kotlin.verify

@ExtendWith(MockitoExtension::class)
internal class BluetoothDataSourceTest {
    private val deviceManager = mock<DeviceManager>()
    private val context = TestContext()

    @Test
    fun `should forward start event to device manager`() {
        BluetoothDataSource(deviceManager, true).start(context)
        verify(deviceManager).start(same(context))
    }

    @Test
    fun `should forward stop event to device manager`() {
        BluetoothDataSource(deviceManager, true).stop(context)
        verify(deviceManager).stop(same(context))
    }

    private class TestContext : PipelineContext {
        override val scheduler: TaskScheduler
            get() = throw NotImplementedError("test")
    }
}
