package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.pipeline.component.DataListener
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock

@ExtendWith(MockitoExtension::class)
internal class ListenerDataSourceTest {

    private val context = mock<PipelineContext>()

    @Test
    fun `should start data source`() {
        val listener = TestListener()
        val dataSource = ListenerDataSource("test", listener, TestDataDecoder())
        dataSource.start(context)

        then(listener.startCalled).isTrue
    }

    @Test
    fun `should stop data source`() {
        val listener = TestListener()
        val dataSource = ListenerDataSource("test", listener, TestDataDecoder())
        dataSource.stop(context)

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

        dataSource.start(context)
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
        override fun decode(data: String): List<Int> = listOf(data.toInt())
    }
}
