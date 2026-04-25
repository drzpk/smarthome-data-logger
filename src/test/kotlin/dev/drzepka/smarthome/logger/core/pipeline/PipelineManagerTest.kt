package dev.drzepka.smarthome.logger.core.pipeline

import dev.drzepka.smarthome.common.TaskScheduler
import dev.drzepka.smarthome.logger.core.pipeline.component.sender.DataSender
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import java.time.Duration

@ExtendWith(MockitoExtension::class)
internal class PipelineManagerTest {

    @Test
    fun `should start pipeline immediately when added`() {
        val pipeline = TestPipeline()
        val manager = createManager()

        manager.addPipeline(pipeline)

        then(pipeline.startCallCount).isEqualTo(1)
        then(pipeline.stopCallCount).isEqualTo(0)
    }

    @Test
    fun `should stop all pipelines when manager is stopped`() {
        val pipeline = TestPipeline()
        val manager = createManager()

        manager.addPipeline(pipeline)
        manager.stop()

        then(pipeline.startCallCount).isEqualTo(1)
        then(pipeline.stopCallCount).isEqualTo(1)
    }

    private fun createManager() = PipelineManager(mock<TaskScheduler>(), mock<DataSender>())

    private class TestPipeline : Pipeline("test", Duration.ofSeconds(1)) {
        var startCallCount = 0
        var stopCallCount = 0

        override fun start(scheduler: TaskScheduler, dataSender: DataSender) {
            startCallCount++
        }

        override fun stop() {
            stopCallCount++
        }
    }
}
