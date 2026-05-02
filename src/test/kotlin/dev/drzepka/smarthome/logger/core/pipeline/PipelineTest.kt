package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.TemperatureMeasurement
import dev.drzepka.smarthome.logger.core.pipeline.component.DataDecoder
import dev.drzepka.smarthome.logger.core.pipeline.component.DataFilter
import dev.drzepka.smarthome.logger.core.pipeline.component.datasource.DataSource
import dev.drzepka.smarthome.logger.core.pipeline.component.sender.DataSender
import org.assertj.core.api.BDDAssertions.assertThatIllegalStateException
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
internal class PipelineTest {

    private val dataSender = mock<DataSender>()
    private val scheduler = mock<TaskScheduler>(defaultAnswer = ReturnsDeepStubs())

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
    fun `should queue collected data`() {
        val pipeline = getPipeline()
        val source = TestDataSource()

        pipeline.addDataSource(source)
        pipeline.start(scheduler, dataSender)

        val m1 = createMeasurement("1")
        val m2 = createMeasurement("2")
        source.generateData(m1, m2)

        val captor = argumentCaptor<Collection<Measurement>>()
        verify(dataSender).queue(captor.capture())
        val queued = captor.firstValue.toList()

        then(queued[0]).isEqualTo(m1)
        then(queued[1]).isEqualTo(m2)
    }

    @Test
    fun `should not queue filtered out data`() {
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
        source.generateData(pass1, dropMeasurement, pass2)

        val captor = argumentCaptor<Collection<Measurement>>()
        verify(dataSender, times(1)).queue(captor.capture())
        val queued = captor.firstValue.toList()

        then(queued).hasSize(2)
        then(queued[0]).isEqualTo(pass1)
        then(queued[1]).isEqualTo(pass2)
    }

    private fun getPipeline(): Pipeline = Pipeline("TestPipeline")

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

        fun generateData(vararg data: Measurement) {
            forwardData(data.toList())
        }
    }

    private class TestFilter : DataFilter {
        override fun filter(data: Measurement): Measurement? = data
    }
}
