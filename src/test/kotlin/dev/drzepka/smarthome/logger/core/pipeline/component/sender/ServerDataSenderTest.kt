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
    fun `should queue items and send them on scheduled interval`() {
        runBlocking {
            val sender = getSender()
            val m1 = measurement("1")
            val m2 = measurement("2")

            sender.queue(listOf(m1, m2))

            verify(scheduler).schedule(any(), any(), taskCaptor.capture())
            taskCaptor.firstValue.invoke()

            verify(executor).sendMeasurements(measurementsCaptor.capture())
            then(measurementsCaptor.firstValue.measurements).containsExactly(m1, m2)
        }
    }

    @Test
    fun `should send items immediately`() {
        runBlocking {
            val sender = getSender()
            val m = measurement("1")

            sender.send(listOf(m))

            verify(executor).sendMeasurements(measurementsCaptor.capture())
            then(measurementsCaptor.firstValue.measurements).containsExactly(m)
        }
    }

    @Test
    fun `should not throw when send fails immediately`() {
        runBlocking {
            whenever(executor.sendMeasurements(any())).thenThrow(RuntimeException("error"))
            val sender = getSender()

            sender.send(listOf(measurement("1")))
        }
    }

    @Test
    fun `should keep batch in queue on connection exception`() {
        runBlocking {
            whenever(executor.sendMeasurements(any())).thenThrow(ConnectionException("url", RuntimeException()))
            val sender = getSender()

            sender.queue(listOf(measurement("1")))

            verify(scheduler).schedule(any(), any(), taskCaptor.capture())
            taskCaptor.firstValue.invoke()
            taskCaptor.firstValue.invoke()

            verify(executor, times(2)).sendMeasurements(any())
        }
    }

    @Test
    fun `should remove batch from queue on non-connection exception`() {
        runBlocking {
            whenever(executor.sendMeasurements(any())).thenThrow(RuntimeException("error"))
            val sender = getSender()

            sender.queue(listOf(measurement("1")))

            verify(scheduler).schedule(any(), any(), taskCaptor.capture())
            taskCaptor.firstValue.invoke()
            taskCaptor.firstValue.invoke()

            verify(executor, times(1)).sendMeasurements(any())
        }
    }

    @Test
    fun `should not throw when scheduled send fails`() {
        runBlocking {
            whenever(executor.sendMeasurements(any())).thenThrow(RuntimeException("error"))
            val sender = getSender()

            sender.queue(listOf(measurement("1")))

            verify(scheduler).schedule(any(), any(), taskCaptor.capture())
            taskCaptor.firstValue.invoke()
        }
    }

    @Test
    fun `should skip scheduled sends after reaching error threshold`() {
        runBlocking {
            whenever(executor.sendMeasurements(any())).thenThrow(ConnectionException("url", RuntimeException()))
            val sender = getSender(errorThreshold = 3, throttleSkipCount = 2)

            sender.queue(listOf(measurement("1")))

            verify(scheduler).schedule(any(), any(), taskCaptor.capture())
            val sendQueued = taskCaptor.firstValue

            // 3 failures to reach threshold
            sendQueued.invoke()
            sendQueued.invoke()
            sendQueued.invoke()

            // next 2 should be skipped (throttleSkipCount = 2)
            sendQueued.invoke()
            sendQueued.invoke()

            verify(executor, times(3)).sendMeasurements(any())
        }
    }

    @Test
    fun `should attempt send again after throttle countdown expires`() {
        runBlocking {
            whenever(executor.sendMeasurements(any())).thenThrow(ConnectionException("url", RuntimeException()))
            val sender = getSender(errorThreshold = 3, throttleSkipCount = 2)

            sender.queue(listOf(measurement("1")))

            verify(scheduler).schedule(any(), any(), taskCaptor.capture())
            val sendQueued = taskCaptor.firstValue

            // 3 failures
            repeat(3) { sendQueued.invoke() }
            // 2 skipped cycles
            repeat(2) { sendQueued.invoke() }
            // 4th actual attempt after throttle
            sendQueued.invoke()

            verify(executor, times(4)).sendMeasurements(any())
        }
    }

    @Test
    fun `should resume normal send interval after recovery`() {
        runBlocking {
            var failCount = 0
            whenever(executor.sendMeasurements(any())).thenAnswer {
                if (failCount++ < 3) throw ConnectionException("url", RuntimeException())
            }
            val sender = getSender(errorThreshold = 3, throttleSkipCount = 2)

            sender.queue(listOf(measurement("1")))

            verify(scheduler).schedule(any(), any(), taskCaptor.capture())
            val sendQueued = taskCaptor.firstValue

            // 3 failures to trigger throttle
            repeat(3) { sendQueued.invoke() }
            // 2 skipped cycles
            repeat(2) { sendQueued.invoke() }
            // successful retry (4th actual attempt) — throttle is cleared
            sendQueued.invoke()

            // queue new items after recovery
            sender.queue(listOf(measurement("2")))
            // next invocation should execute (not skip)
            sendQueued.invoke()

            verify(executor, times(5)).sendMeasurements(any())
        }
    }

    @Test
    fun `should not throttle immediate sends`() {
        runBlocking {
            whenever(executor.sendMeasurements(any())).thenThrow(ConnectionException("url", RuntimeException()))
            val sender = getSender(errorThreshold = 3, throttleSkipCount = 2)

            sender.queue(listOf(measurement("1")))

            verify(scheduler).schedule(any(), any(), taskCaptor.capture())
            repeat(3) { taskCaptor.firstValue.invoke() }

            // immediate sends should still execute regardless of throttle state
            sender.send(listOf(measurement("2")))

            verify(executor, times(4)).sendMeasurements(any())
        }
    }

    private fun getSender(errorThreshold: Int = 3, throttleSkipCount: Int = 4): ServerDataSender {
        val props = "server.sender.errorThreshold=$errorThreshold\n" +
                "server.sender.sendInterval=PT1S\n" +
                "server.sender.throttleDelay=PT${throttleSkipCount}S\n" +
                "server.sender.maxThrottleDelay=PT${throttleSkipCount * 100}S"
        return ServerDataSender(
            ServerDataSenderProperties(PropertiesConfigPropertySource(props)),
            executor,
            scheduler
        )
    }

    private fun measurement(mac: String): Measurement =
        TemperatureMeasurement(mac = mac, temperature = BigDecimal("25.0"))
}
