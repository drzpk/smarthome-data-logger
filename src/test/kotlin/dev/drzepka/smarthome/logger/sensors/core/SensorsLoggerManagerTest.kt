package dev.drzepka.smarthome.logger.sensors.core

import dev.drzepka.smarthome.logger.core.queue.LoggerQueue
import dev.drzepka.smarthome.logger.core.queue.ProcessingResult
import dev.drzepka.smarthome.logger.core.queue.QueueItem
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.BluetoothServiceData
import dev.drzepka.smarthome.logger.sensors.model.bluetooth.MacAddress
import dev.drzepka.smarthome.logger.sensors.model.server.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.sensors.model.server.Device
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
    private val queue = mock<LoggerQueue<CreateMeasurementsRequest.Measurement>>()

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

        val queueItem1 = QueueItem(CreateMeasurementsRequest.Measurement(), Instant.now().minusSeconds(3))
        val queueItem2 = QueueItem(CreateMeasurementsRequest.Measurement(), Instant.now().minusSeconds(2))

        var processingNo = 0
        whenever(queue.processQueue(any())).doSuspendableAnswer {
            val handler: (suspend (batch: Collection<QueueItem<CreateMeasurementsRequest.Measurement>>) -> ProcessingResult) =
                it.getArgument(0)

            if (processingNo++ == 0)
                handler.invoke(listOf(queueItem1))
            else
                handler.invoke(listOf(queueItem2))

            Unit
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

        Unit
    }

    @Test
    fun `should stop sending measurements if time limit has been exceeded`() = runBlocking {
        val device = createDevice("dev")
        whenever(executor.getDevices()).doReturn(listOf(device))

        val queueItem1 = QueueItem(CreateMeasurementsRequest.Measurement(), Instant.now().minusSeconds(3))
        val queueItem2 = QueueItem(CreateMeasurementsRequest.Measurement(), Instant.now().minusSeconds(2))

        var processingNo = 0
        whenever(queue.processQueue(any())).doSuspendableAnswer {
            val handler: (suspend (batch: Collection<QueueItem<CreateMeasurementsRequest.Measurement>>) -> ProcessingResult) =
                it.getArgument(0)

            delay(100)
            if (processingNo++ == 0)
                handler.invoke(listOf(queueItem1))
            else
                handler.invoke(listOf(queueItem2))

            Unit
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

    private fun createDevice(mac: String): Device = Device().apply {
        id = 1
        this.mac = mac
    }

    private fun createBluetoothServiceData(mac: String): BluetoothServiceData =
        BluetoothServiceData(MacAddress(mac), "11 11 11 11 11 11 11 11 11 11 11 11 11 11")

    private fun getManager(): SensorsLoggerManager = SensorsLoggerManager(executor, queue)
}