package dev.drzepka.smarthome.logger.sensors.pipeline

import dev.drzepka.smarthome.logger.core.model.measurement.CreateMeasurementsRequest
import dev.drzepka.smarthome.logger.core.model.measurement.TemperatureMeasurement
import dev.drzepka.smarthome.logger.core.network.SensorsRequestExecutor
import dev.drzepka.smarthome.logger.core.queue.QueueItem
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
internal class SensorsDataSenderTest {

    private val executor = mock<SensorsRequestExecutor>()
    private val captor = argumentCaptor<CreateMeasurementsRequest>()

    @Test
    fun `should send items`() = runBlocking {
        val item1 = QueueItem(
            content = TemperatureMeasurement(mac = "1", temperature = BigDecimal("25.41")),
            createdAt = getTime(10)
        )
        val item2 = QueueItem(
            content=TemperatureMeasurement(mac = "2", temperature = BigDecimal("23.33")),
            createdAt = getTime(20)
        )

        SensorsDataSender(executor).send(listOf(item1, item2))

        verify(executor).sendMeasurements(captor.capture())
        val request = captor.firstValue

        then(request.measurements[0]).isEqualTo(item1.content)
        then(request.measurements[1]).isEqualTo(item2.content)

        Unit
    }

    private fun getTime(secondsIntoPast: Int): Instant =
        LocalDateTime
            .now()
            .minusSeconds(secondsIntoPast.toLong())
            .atZone(ZoneId.systemDefault())
            .toInstant()
}
