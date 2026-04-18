package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.pipeline.component.DataListener
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class ListenerDataSourceTest {

    @Test
    fun `should start data source`() {
        val listener = TestListener()
        val dataSource = ListenerDataSource("test", listener, TestDataDecoder())
        dataSource.start()

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

        val receivedData = mutableListOf<Int>()
        dataSource.receiver = object : DataReceiver<Int> {
            override fun onDataAvailable(items: Collection<Int>) {
                receivedData.addAll(items)
            }
        }

        dataSource.start()
        listener.generateTestData()

        then(receivedData).containsExactly(123)
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

    private class TestDataDecoder : DataDecoder<String, Int> {
        override fun decode(item: String): Collection<Int> = listOf(item.toInt())
    }
}
