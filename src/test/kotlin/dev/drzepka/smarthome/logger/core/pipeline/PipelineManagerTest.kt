package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.common.TaskScheduler
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import java.time.Duration

@ExtendWith(MockitoExtension::class)
internal class PipelineManagerTest {

    private val scheduler = mock<TaskScheduler>()

    @Test
    fun `should start pipeline immediately when added`() {
        val pipeline = TestPipeline()
        val manager = PipelineManager(scheduler)

        manager.addPipeline(pipeline)

        then(pipeline.startCallCount).isEqualTo(1)
        then(pipeline.stopCallCount).isEqualTo(0)
    }

    @Test
    fun `should stop all pipelines when manager is stopped`() {
        val pipeline = TestPipeline()
        val manager = PipelineManager(scheduler)

        manager.addPipeline(pipeline)
        manager.stop()

        then(pipeline.startCallCount).isEqualTo(1)
        then(pipeline.stopCallCount).isEqualTo(1)
    }


    private class TestPipeline : Pipeline<String>("test", Duration.ofSeconds(1), mock()) {
        var startCallCount = 0
        var stopCallCount = 0

        override fun start(context: PipelineContext) {
            startCallCount++
        }

        override fun stop(context: PipelineContext) {
            stopCallCount++
        }
    }
}
