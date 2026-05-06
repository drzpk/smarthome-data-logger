package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.TemperatureMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.pipeline.component.DataListener
import dev.drzepka.smarthome.logger.core.scheduler.TaskScheduler
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
internal class ListenerDataSourceTest {

    @Test
    fun `should start data source`() {
        val listener = TestListener()
        val dataSource = ListenerDataSource("test", listener, TestDataDecoder())
        dataSource.start(mock<TaskScheduler>())

        then(listener.startCalled).isTrue
    }

    @Test
    fun `should stop data source`() {
        val listener = TestListener()
        val dataSource = ListenerDataSource("test", listener, TestDataDecoder())
        dataSource.stop()

        then(listener.stopCalled).isTrue
    }

    @Test
    fun `should decode and forward data`() {
        val listener = TestListener()
        val dataSource = ListenerDataSource("test", listener, TestDataDecoder())

        val receivedData = mutableListOf<Measurement>()
        dataSource.receiver = object : DataReceiver {
            override fun onDataAvailable(items: Collection<Measurement>) {
                receivedData.addAll(items)
            }
        }

        dataSource.start(mock<TaskScheduler>())
        listener.generateTestData()

        then(receivedData).hasSize(1)
        then(receivedData.first().mac).isEqualTo("123")
    }

    private class TestListener : DataListener<String>() {
        var startCalled = false
        var stopCalled = false

        override fun start() {
            startCalled = true
        }

        override fun stop() {
            stopCalled = true
        }

        fun generateTestData() {
            onDataReceived("123")
        }
    }

    private class TestDataDecoder : DataDecoder<String> {
        override fun decode(item: String): Collection<Measurement> =
            listOf(TemperatureMeasurement(mac = item, temperature = BigDecimal(item.toInt())))
    }
}
