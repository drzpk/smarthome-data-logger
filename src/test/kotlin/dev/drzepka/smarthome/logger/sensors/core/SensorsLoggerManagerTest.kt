package dev.drzepka.smarthome.logger.sensors.core

import dev.drzepka.smarthome.logger.core.executor.ConnectionException
import dev.drzepka.smarthome.logger.core.queue.LoggerQueue
import dev.drzepka.smarthome.logger.core.queue.QueueBatch
import dev.drzepka.smarthome.logger.core.queue.QueueItem
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.sensors.model.server.CreateMeasurementsResponse
import dev.drzepka.smarthome.logger.sensors.model.server.Device
import dev.drzepka.smarthome.logger.sensors.model.server.Measurement
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(MockitoExtension::class)
internal class SensorsLoggerManagerTest {

    private val executor = mock<SensorsRequestExecutor>()
    private val queue = mock<LoggerQueue<Measurement>>()

    @Test
    fun `should enqueue request created from known device's bluetooth data`() = runBlocking {
        val device = createDevice("ab:cd:12")
        whenever(executor.getDevices()).doReturn(listOf(device))

        val manager = getManager()
        manager.refreshDevices()
        manager.decodeBluetoothData(createBluetoothServiceData("AB:CD:12"))
        manager.decodeBluetoothData(BluetoothServiceData(MacAddress("99:33:11"), ""))

        verify(queue, times(1)).enqueue(any())
    }

    @Test
    fun `should send measurements to server`() = runBlocking {
        val device = createDevice("dev")
        whenever(executor.getDevices()).doReturn(listOf(device))

        val queueItem1 = QueueItem(Measurement(), Instant.now().minusSeconds(3))
        val queueItem2 = QueueItem(Measurement(), Instant.now().minusSeconds(2))
        val queueBatch1 = QueueBatch(listOf(queueItem1))
        val queueBatch2 = QueueBatch(listOf(queueItem2))

        var processingNo = 0
        whenever(queue.getBatch()).doAnswer {
            if (processingNo++ == 0)
                return@doAnswer queueBatch1
            else
                return@doAnswer queueBatch2
        }
        whenever(queue.size()).thenAnswer {
            2 - processingNo
        }

        val manager = getManager()
        manager.refreshDevices()
        manager.decodeBluetoothData(createBluetoothServiceData("dev"))
        manager.decodeBluetoothData(createBluetoothServiceData("dev"))

        val now = Instant.now()
        manager.sendMeasurementsToServer(Duration.ofSeconds(1))

        then(processingNo).isEqualTo(2)

        val captor = argumentCaptor<CreateMeasurementsRequest>()
        verify(executor, times(2)).sendMeasurements(captor.capture())

        val item1Offset = ChronoUnit.MILLIS.between(queueItem1.createdAt, now)
        val item2Offset = ChronoUnit.MILLIS.between(queueItem2.createdAt, now)
        val itemOffsetUncertainty = Offset.offset(150L)

        val requests = captor.allValues
        then(requests[0].measurements).hasSize(1)
        then(requests[0].measurements[0].timestampOffsetMillis).isCloseTo(item1Offset, itemOffsetUncertainty)
        then(requests[1].measurements).hasSize(1)
        then(requests[1].measurements[0].timestampOffsetMillis).isCloseTo(item2Offset, itemOffsetUncertainty)

        verify(queue).removeBatch(same(queueBatch1))
        verify(queue).removeBatch(same(queueBatch2))
    }

    @Test
    fun `should stop sending measurements if time limit has been exceeded`() = runBlocking {
        val device = createDevice("dev")
        whenever(executor.getDevices()).doReturn(listOf(device))

        val queueItem1 = QueueItem(Measurement(), Instant.now().minusSeconds(3))
        val queueItem2 = QueueItem(Measurement(), Instant.now().minusSeconds(2))

        var processingNo = 0
        whenever(queue.getBatch()).doAnswer {
            Thread.sleep(100)
            if (processingNo++ == 0)
                return@doAnswer QueueBatch(listOf(queueItem1))
            else
                return@doAnswer QueueBatch(listOf(queueItem2))
        }
        whenever(queue.size()).thenAnswer {
            2 - processingNo
        }

        val manager = getManager()
        manager.refreshDevices()
        manager.decodeBluetoothData(createBluetoothServiceData("dev"))
        manager.decodeBluetoothData(createBluetoothServiceData("dev"))

        manager.sendMeasurementsToServer(Duration.ofMillis(100))

        then(processingNo).isEqualTo(1)

        Unit
    }

    @Test
    fun `should not remove batch on connection exception`() = runBlocking{
        val device = createDevice("dev")
        whenever(executor.getDevices()).doReturn(listOf(device))

        val exception = ConnectionException("url", IllegalArgumentException("test"))
        whenever(executor.sendMeasurements(any())).thenThrow(exception)

        val manager = getManager()
        manager.refreshDevices()

        val item = QueueItem(Measurement(), Instant.now().minusSeconds(2))
        val batch = QueueBatch(listOf(item))
        whenever(queue.getBatch()).thenReturn(batch)
        whenever(queue.size()).thenReturn(1)

        val caught = kotlin.runCatching {
            manager.sendMeasurementsToServer(Duration.ofSeconds(3))
        }

        then(caught.exceptionOrNull()).isSameAs(exception)
        verify(queue, times(0)).removeBatch(same(batch))
    }

    @Test
    fun `should remove batch on exception`() = runBlocking{
        val device = createDevice("dev")
        whenever(executor.getDevices()).doReturn(listOf(device))

        val exception = IllegalStateException("something wrong happened")
        whenever(executor.sendMeasurements(any())).thenThrow(exception)

        val manager = getManager()
        manager.refreshDevices()

        val item = QueueItem(Measurement(), Instant.now().minusSeconds(2))
        val batch = QueueBatch(listOf(item))
        whenever(queue.getBatch()).thenReturn(batch)
        whenever(queue.size()).thenReturn(1)

        val caught = kotlin.runCatching {
            manager.sendMeasurementsToServer(Duration.ofSeconds(3))
        }

        then(caught.exceptionOrNull()).isSameAs(exception)
        verify(queue).removeBatch(same(batch))
    }

    @Test
    fun `should prevent infinite loop when sending measurements`() = runBlocking{
        val device = createDevice("dev")
        whenever(executor.getDevices()).doReturn(listOf(device))

        val item = QueueItem(Measurement(), Instant.now().minusSeconds(2))
        val batch = QueueBatch(listOf(item))
        whenever(queue.getBatch()).thenReturn(batch)
        whenever(queue.size()).thenReturn(1) // Always return 1 - cause infinite loop

        whenever(executor.sendMeasurements(any())).doSuspendableAnswer {
            delay(1) // Create suspension point - required for withTimeout to work
            CreateMeasurementsResponse()
        }

        val manager = getManager()
        manager.refreshDevices()

        val result = runCatching {
            withTimeout(1000) {
                manager.sendMeasurementsToServer(Duration.ofSeconds(30))
            }
        }

        then(result.isSuccess).isTrue

        Unit
    }

    private fun createDevice(mac: String): Device = Device().apply {
        id = 1
        this.mac = mac
    }

    private fun createBluetoothServiceData(mac: String): BluetoothServiceData =
        BluetoothServiceData(MacAddress(mac), "11 11 11 11 11 11 11 11 11 11 11 11 11 11")

    private fun getManager(): SensorsLoggerManager = SensorsLoggerManager(executor, queue)
}