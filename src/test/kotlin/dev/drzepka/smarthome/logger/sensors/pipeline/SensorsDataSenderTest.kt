package dev.drzepka.smarthome.logger.sensors.pipeline

import dev.drzepka.smarthome.logger.core.queue.QueueItem
import dev.drzepka.smarthome.logger.sensors.core.SensorsRequestExecutor
import dev.drzepka.smarthome.logger.sensors.model.server.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.sensors.model.server.Measurement
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.BDDAssertions.then
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
internal class SensorsDataSenderTest {

    private val executor = mock<SensorsRequestExecutor>()
    private val captor = argumentCaptor<CreateMeasurementsRequest>()

    @Test
    fun `should send items`() = runBlocking {
        val item1 = QueueItem(Measurement(), createdAt = getTime(10))
        val item2 = QueueItem(Measurement(), createdAt = getTime(20))

        SensorsDataSender(executor).send(listOf(item1, item2))

        verify(executor).sendMeasurements(captor.capture())
        val request = captor.firstValue

        then(request.measurements[0].timestampOffsetMillis).isCloseTo(10_000L, Offset.offset(100L))
        then(request.measurements[1].timestampOffsetMillis).isCloseTo(20_000L, Offset.offset(100L))

        Unit
    }

    private fun getTime(secondsIntoPast: Int): Instant =
        LocalDateTime
            .now()
            .minusSeconds(secondsIntoPast.toLong())
            .atZone(ZoneId.systemDefault())
            .toInstant()
}
