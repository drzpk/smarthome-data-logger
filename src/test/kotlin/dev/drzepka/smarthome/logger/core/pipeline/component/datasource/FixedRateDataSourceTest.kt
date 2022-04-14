package dev.drzepka.smarthome.logger.core.pipeline.component.datasource

import dev.drzepka.smarthome.logger.core.pipeline.PipelineContext
import dev.drzepka.smarthome.logger.core.pipeline.component.DataCollector
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class FixedRateDataSourceTest {

    private val collector = mock<DataCollector<String>>()
    private val context = mock<PipelineContext>(defaultAnswer = ReturnsDeepStubs())
    private val schedulerTaskCaptor = argumentCaptor<suspend () -> Unit>()

    @Test
    fun `should start data source`() {
        val interval = Duration.ofSeconds(10L)
        val dataSource = FixedRateDataSource("test", interval, collector, TestDecoder())

        dataSource.start(context)

        verify(collector).start()
        verify(context.scheduler).schedule(argThat { endsWith("test") }, eq(interval), any())
    }

    @Test
    fun `should stop data source`() {
        val interval = Duration.ofSeconds(10L)
        val dataSource = FixedRateDataSource("test", interval, collector, TestDecoder())

        dataSource.stop(context)

        verify(collector).stop()
        verify(context.scheduler).cancel(argThat { endsWith("test") })
    }

    @Test
    fun `should decode and forward data`() = runBlockingTest {
        val dataSource = FixedRateDataSource("test", Duration.ofSeconds(1), collector, TestDecoder())
        dataSource.start(context)

        var receiverCalled = false
        dataSource.receiver = object : DataReceiver<Int> {
            override fun onDataAvailable(items: Collection<Int>) {
                receiverCalled = true
            }
        }

        verify(context.scheduler).schedule(any(), any(), schedulerTaskCaptor.capture())
        schedulerTaskCaptor.firstValue.invoke()

        verify(collector).getData()
        then(receiverCalled).isTrue()
    }

    private class TestDecoder : DataDecoder<String, Int> {
        override fun decode(data: String): List<Int> = listOf(data.toInt())
    }
}
