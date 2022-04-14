package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.logger.core.executor.ConnectionException
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.pipeline.component.DataFilter
import dev.drzepka.smarthome.logger.core.pipeline.component.DataSender
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.DataSource
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
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class PipelineTest {

    private val dataSender = mock<DataSender<String>>()
    //private val scheduler = mock<TaskScheduler>()
    private val context = mock<PipelineContext>(defaultAnswer = ReturnsDeepStubs())
    private val sendInterval = Duration.ofSeconds(1)

    private val taskCaptor = argumentCaptor<suspend () -> Unit>()
    private val queueItemsCaptor = argumentCaptor<Collection<QueueItem<String>>>()

    private val queue = spy<LoggerQueue<String>>(LoggerQueue(5, Duration.ofHours(1)))

//    private val context: PipelineContext
//        get() = object : PipelineContext {
//            override val scheduler = this@PipelineTest.scheduler
//        }

    @Test
    fun `should add data source to pipeline, set its receiver, and forward start-stop events`() {
        val pipeline = getPipeline()
        val source = TestDataSource()

        pipeline.addDataSource(source)
        pipeline.start(context)
        pipeline.stop(context)

        then(source.startCallCount).isEqualTo(1)
        then(source.stopCallCount).isEqualTo(1)
        then(source.receiver).isNotNull
    }

    @Test
    fun `should prevent from adding data source when pipeline is running`() {
        val pipeline = getPipeline()
        pipeline.start(context)

        assertThatIllegalStateException()
            .isThrownBy { pipeline.addDataSource(TestDataSource()) }
            .withMessage("Cannot modify pipeline state when it's running")
    }

    @Test
    fun `should add filter to pipeline, set its receiver, and forward start-stop events`() {
        val pipeline = getPipeline()
        val filter = TestFilter()

        pipeline.addFilter(filter)
        pipeline.start(context)
        pipeline.stop(context)

        then(filter.startCallCount).isEqualTo(1)
        then(filter.stopCallCount).isEqualTo(1)
    }

    @Test
    fun `should prevent from adding filter when pipeline is running`() {
        val pipeline = getPipeline()
        pipeline.start(context)

        val filter = object : DataFilter<String> {
            override fun filter(data: String): String? = null
        }

        assertThatIllegalStateException()
            .isThrownBy { pipeline.addFilter(filter) }
            .withMessage("Cannot modify pipeline state when it's running")
    }

    @Test
    fun `should send collected data to sender`() = runBlockingTest {
        val pipeline = getPipeline()
        val source = TestDataSource()

        pipeline.addDataSource(source)
        pipeline.start(context)

        source.generateData("string1")
        source.generateData("string2")

        verify(context.scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        verify(dataSender).send(queueItemsCaptor.capture())
        val sentItems = ArrayList(queueItemsCaptor.firstValue)

        then(sentItems[0].content).isEqualTo("string1")
        then(sentItems[1].content).isEqualTo("string2")
    }

    @Test
    fun `should not send filtered out data`() = runBlockingTest {
        val pipeline = getPipeline()
        val source = TestDataSource()

        val filter = object : DataFilter<String> {
            override fun filter(data: String): String? = if (data != "drop") data else null
        }

        pipeline.addDataSource(source)
        pipeline.addFilter(filter)
        pipeline.start(context)

        source.generateData("pass1")
        source.generateData("drop")
        source.generateData("pass2")

        verify(context.scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        verify(dataSender).send(queueItemsCaptor.capture())
        val sentItems = ArrayList(queueItemsCaptor.firstValue)

        then(sentItems[0].content).isEqualTo("pass1")
        then(sentItems[1].content).isEqualTo("pass2")
    }

    @Test
    fun `should stop sending data to sender if time limit has been exceeded`() = runBlockingTest {
        val queueItem1 = QueueItem("item1", Instant.now().minusSeconds(3))
        val queueItem2 = QueueItem("item2", Instant.now().minusSeconds(2))

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
        pipeline.start(context)

        verify(context.scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        then(processingNo).isEqualTo(1)

        Unit
    }

    @Test
    fun `should not remove batch passed to sender on connection exception`() = runBlockingTest {
        val exception = ConnectionException("url", IllegalArgumentException("test"))
        whenever(dataSender.send(any())).thenThrow(exception)

        val item = QueueItem("item", Instant.now().minusSeconds(2))
        val batch = QueueBatch(listOf(item))
        whenever(queue.getBatch()).thenReturn(batch)
        whenever(queue.size()).thenReturn(1)

        val pipeline = getPipeline()
        pipeline.start(context)

        verify(context.scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        verify(queue, times(0)).removeBatch(same(batch))
    }

    @Test
    fun `should remove batch on any other exception`() = runBlockingTest {
        val exception = IllegalStateException("something went wrong")
        whenever(dataSender.send(any())).thenThrow(exception)

        val item = QueueItem("item", Instant.now().minusSeconds(2))
        val batch = QueueBatch(listOf(item))
        whenever(queue.getBatch()).thenReturn(batch)
        whenever(queue.size()).thenReturn(1)

        val pipeline = getPipeline()
        pipeline.start(context)

        verify(context.scheduler).schedule(any(), any(), taskCaptor.capture())
        taskCaptor.firstValue.invoke()

        verify(queue).removeBatch(same(batch))
    }

    private fun getPipeline(): Pipeline<String> = Pipeline("TestPipeline", sendInterval, dataSender, queue)

    private class TestDataSource : DataSource<String, String>("TestSource", NoopDecoder()) {
        var startCallCount = 0
        var stopCallCount = 0

        override fun start(context: PipelineContext) {
            super.start(context)
            startCallCount++
        }

        override fun stop(context: PipelineContext) {
            super.stop(context)
            stopCallCount++
        }

        fun generateData(data: String) {
            forwardData(listOf(data))
        }
    }

    private open class TestFilter : DataFilter<String> {
        var startCallCount = 0
        var stopCallCount = 0

        override fun start(context: PipelineContext) {
            super.start(context)
            startCallCount++
        }

        override fun stop(context: PipelineContext) {
            super.stop(context)
            stopCallCount++
        }

        override fun filter(data: String): String? = null
    }

    private class NoopDecoder : DataDecoder<String, String> {
        override fun decode(data: String): List<String> = listOf(data)
    }
}
