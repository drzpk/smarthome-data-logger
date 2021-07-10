package dev.drzepka.smarthome.logger.sensors.core

import dev.drzepka.smarthome.common.TaskScheduler
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
internal class SensorsLoggerOrchestratorTest {

    private val manager = mock<SensorsLoggerManager>()
    private val scheduler = mock<TaskScheduler>()

    @Test
    fun `should start orchestrator`() = runBlocking {
        getOrchestrator().start()

        verify(scheduler).schedule(eq("sensors_deviceRefresh"), any(), any())
        verify(scheduler).schedule(eq("sensors_measurementSend"), any(), any())
    }

    private fun getOrchestrator(): SensorsLoggerOrchestrator =
        SensorsLoggerOrchestrator(manager, scheduler, true)
}