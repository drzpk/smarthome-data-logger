package dev.drzepka.smarthome.logger.core.pipeline.component.sender

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.config.PropertiesConfigPropertySource
import dev.drzepka.smarthome.logger.core.config.ServerDataSenderProperties
import dev.drzepka.smarthome.logger.core.executor.ConnectionException
import dev.drzepka.smarthome.logger.core.model.measurement.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.TemperatureMeasurement
import dev.drzepka.smarthome.logger.core.transport.ServerRequestExecutor
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
internal class ServerDataSenderTest {

    private val executor = mock<ServerRequestExecutor>()
    private val scheduler = mock<TaskScheduler>(defaultAnswer = ReturnsDeepStubs())
    private val measurementsCaptor = argumentCaptor<CreateMeasurementsRequest>()
    private val taskCaptor = argumentCaptor<suspend () -> Unit>()

    @Test
    fun `should queue items and send them on scheduled interval`() = runBlocking {
        val sender = getSender()
        val m1 = measurement("1")
        val m2 = measurement("2")

        sender.queue(listOf(m1, m2))

        verify(scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        verify(executor).sendMeasurements(measurementsCaptor.capture())
        then(measurementsCaptor.firstValue.measurements).containsExactly(m1, m2)
    }

    @Test
    fun `should send items immediately`() = runBlocking {
        val sender = getSender()
        val m = measurement("1")

        sender.send(listOf(m))

        verify(executor).sendMeasurements(measurementsCaptor.capture())
        then(measurementsCaptor.firstValue.measurements).containsExactly(m)
    }

    @Test
    fun `should not throw when send fails immediately`() = runBlocking {
        whenever(executor.sendMeasurements(any())).thenThrow(RuntimeException("error"))
        val sender = getSender()

        sender.send(listOf(measurement("1")))

        Unit
    }

    @Test
    fun `should keep batch in queue on connection exception`() = runBlocking {
        whenever(executor.sendMeasurements(any())).thenThrow(ConnectionException("url", RuntimeException()))
        val sender = getSender()

        sender.queue(listOf(measurement("1")))

        verify(scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()
        taskCaptor.firstValue.invoke()

        verify(executor, times(2)).sendMeasurements(any())
    }

    @Test
    fun `should remove batch from queue on non-connection exception`() = runBlocking {
        whenever(executor.sendMeasurements(any())).thenThrow(RuntimeException("error"))
        val sender = getSender()

        sender.queue(listOf(measurement("1")))

        verify(scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()
        taskCaptor.firstValue.invoke()

        verify(executor, times(1)).sendMeasurements(any())
    }

    @Test
    fun `should not throw when scheduled send fails`() = runBlocking {
        whenever(executor.sendMeasurements(any())).thenThrow(RuntimeException("error"))
        val sender = getSender()

        sender.queue(listOf(measurement("1")))

        verify(scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        Unit
    }

    private fun getSender(): ServerDataSender =
        ServerDataSender(defaultProperties(), executor, scheduler)

    private fun defaultProperties(): ServerDataSenderProperties =
        ServerDataSenderProperties(PropertiesConfigPropertySource(""))

    private fun measurement(mac: String): Measurement =
        TemperatureMeasurement(mac = mac, temperature = BigDecimal("25.0"))
}
