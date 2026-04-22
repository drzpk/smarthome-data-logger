package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.TemperatureMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class FixedRateDataSourceTest {

    private val collector = mock<DataCollector<Measurement>>()
    private val scheduler = mock<TaskScheduler>()
    private val schedulerTaskCaptor = argumentCaptor<suspend () -> Unit>()

    @Test
    fun `should start data source`() {
        val interval = Duration.ofSeconds(10L)
        val dataSource = FixedRateDataSource("test", interval, collector, TestDecoder())

        dataSource.start(scheduler)

        verify(scheduler).schedule(argThat { endsWith("test") }, eq(interval), any())
    }

    @Test
    fun `should stop data source`() {
        val interval = Duration.ofSeconds(10L)
        val dataSource = FixedRateDataSource("test", interval, collector, TestDecoder())

        dataSource.start(scheduler)
        dataSource.stop()

        verify(scheduler).cancel(argThat { endsWith("test") })
    }

    @Test
    fun `should decode and forward data`() = runBlockingTest {
        val dataSource = FixedRateDataSource("test", Duration.ofSeconds(1), collector, TestDecoder())
        dataSource.start(scheduler)

        var receiverCalled = false
        dataSource.receiver = object : DataReceiver {
            override fun onDataAvailable(items: Collection<Measurement>) {
                receiverCalled = true
            }
        }

        val m1 = TemperatureMeasurement(mac = "1", temperature = BigDecimal.ONE)
        val m2 = TemperatureMeasurement(mac = "2", temperature = BigDecimal("2"))
        whenever(collector.getData()).thenReturn(listOf(m1, m2))
        verify(scheduler).schedule(any(), any(), schedulerTaskCaptor.capture())
        schedulerTaskCaptor.firstValue.invoke()

        verify(collector).getData()
        then(receiverCalled).isTrue()
    }

    private class TestDecoder : DataDecoder<Measurement> {
        override fun decode(item: Measurement): Collection<Measurement> = listOf(item)
    }
}
