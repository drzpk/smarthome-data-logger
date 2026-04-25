package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.executor.ConnectionException
import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.TemperatureMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.pipeline.component.DataFilter
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.DataSource
import dev.drzepka.smarthome.logger.core.pipeline.component.sender.DataSender
import dev.drzepka.smarthome.logger.core.queue.LoggerQueue
import dev.drzepka.smarthome.logger.core.queue.QueueBatch
import dev.drzepka.smarthome.logger.core.queue.QueueItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.BDDAssertions.assertThatIllegalStateException
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class PipelineTest {

    private val dataSender = mock<DataSender>()
    private val scheduler = mock<TaskScheduler>(defaultAnswer = ReturnsDeepStubs())
    private val sendInterval = Duration.ofSeconds(1)

    private val taskCaptor = argumentCaptor<suspend () -> Unit>()
    private val queueItemsCaptor = argumentCaptor<Collection<QueueItem>>()

    private val queue = spy(LoggerQueue(5, Duration.ofHours(1)))

    @Test
    fun `should add data source to pipeline, set its receiver, and forward start-stop events`() {
        val pipeline = getPipeline()
        val source = TestDataSource()

        pipeline.addDataSource(source)
        pipeline.start(scheduler, dataSender)
        pipeline.stop()

        then(source.startCallCount).isEqualTo(1)
        then(source.stopCallCount).isEqualTo(1)
        then(source.receiver).isNotNull
    }

    @Test
    fun `should prevent from adding data source when pipeline is running`() {
        val pipeline = getPipeline()
        pipeline.start(scheduler, dataSender)

        pipeline.start(scheduler, dataSender)

        assertThatIllegalStateException()
            .isThrownBy { pipeline.addDataSource(TestDataSource()) }
            .withMessage("Cannot modify pipeline state when it's running")
    }

    @Test
    fun `should add filter to pipeline`() {
        val pipeline = getPipeline()
        pipeline.addFilter(TestFilter())
        pipeline.start(scheduler, dataSender)
        pipeline.stop()
    }

    @Test
    fun `should prevent from adding filter when pipeline is running`() {
        val pipeline = getPipeline()
        pipeline.start(scheduler, dataSender)

        assertThatIllegalStateException()
            .isThrownBy { pipeline.addFilter(object : DataFilter {
                override fun filter(data: Measurement): Measurement? = null
            }) }
            .withMessage("Cannot modify pipeline state when it's running")
    }

    @Test
    fun `should send collected data to sender`() = runBlockingTest {
        val pipeline = getPipeline()
        val source = TestDataSource()

        pipeline.addDataSource(source)
        pipeline.start(scheduler, dataSender)

        val m1 = createMeasurement("1")
        val m2 = createMeasurement("2")
        source.generateData(m1)
        source.generateData(m2)

        verify(scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        verify(dataSender).send(queueItemsCaptor.capture())
        val sentItems = ArrayList(queueItemsCaptor.firstValue)

        then(sentItems[0].content).isEqualTo(m1)
        then(sentItems[1].content).isEqualTo(m2)
    }

    @Test
    fun `should not send filtered out data`() = runBlockingTest {
        val pipeline = getPipeline()
        val source = TestDataSource()
        val dropMeasurement = createMeasurement("drop")

        val filter = object : DataFilter {
            override fun filter(data: Measurement): Measurement? = if (data != dropMeasurement) data else null
        }

        pipeline.addDataSource(source)
        pipeline.addFilter(filter)
        pipeline.start(scheduler, dataSender)

        val pass1 = createMeasurement("pass1")
        val pass2 = createMeasurement("pass2")
        source.generateData(pass1)
        source.generateData(dropMeasurement)
        source.generateData(pass2)

        verify(scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        verify(dataSender).send(queueItemsCaptor.capture())
        val sentItems = ArrayList(queueItemsCaptor.firstValue)

        then(sentItems[0].content).isEqualTo(pass1)
        then(sentItems[1].content).isEqualTo(pass2)
    }

    @Test
    fun `should stop sending data to sender if time limit has been exceeded`() = runBlockingTest {
        val queueItem1 = QueueItem(createMeasurement(), Instant.now().minusSeconds(3))
        val queueItem2 = QueueItem(createMeasurement(), Instant.now().minusSeconds(2))

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

        val pipeline = getPipeline()
        pipeline.start(scheduler, dataSender)

        verify(scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        then(processingNo).isEqualTo(1)
    }

    @Test
    fun `should not remove batch passed to sender on connection exception`() = runBlockingTest {
        val exception = ConnectionException("url", IllegalArgumentException("test"))
        whenever(dataSender.send(any())).thenThrow(exception)

        val item = QueueItem(createMeasurement(), Instant.now().minusSeconds(2))
        val batch = QueueBatch(listOf(item))
        whenever(queue.getBatch()).thenReturn(batch)
        whenever(queue.size()).thenReturn(1)

        val pipeline = getPipeline()
        pipeline.start(scheduler, dataSender)

        verify(scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        verify(queue, times(0)).removeBatch(same(batch))
    }

    @Test
    fun `should remove batch on any other exception`() = runBlockingTest {
        val exception = IllegalStateException("something went wrong")
        whenever(dataSender.send(any())).thenThrow(exception)

        val item = QueueItem(createMeasurement(), Instant.now().minusSeconds(2))
        val batch = QueueBatch(listOf(item))
        whenever(queue.getBatch()).thenReturn(batch)
        whenever(queue.size()).thenReturn(1)

        val pipeline = getPipeline()
        pipeline.start(scheduler, dataSender)

        verify(scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        verify(queue).removeBatch(same(batch))
    }

    private fun getPipeline(): Pipeline = Pipeline("TestPipeline", sendInterval, queue)

    private fun createMeasurement(mac: String = "test"): Measurement =
        TemperatureMeasurement(mac = mac, temperature = BigDecimal.ZERO)

    private class TestDataSource : DataSource<Measurement>("TestSource", DataDecoder.noop<Measurement>()) {
        var startCallCount = 0
        var stopCallCount = 0

        override fun start(scheduler: TaskScheduler) {
            super.start(scheduler)
            startCallCount++
        }

        override fun stop() {
            super.stop()
            stopCallCount++
        }

        fun generateData(data: Measurement) {
            forwardData(listOf(data))
        }
    }

    private class TestFilter : DataFilter {
        override fun filter(data: Measurement): Measurement? = data
    }
}
