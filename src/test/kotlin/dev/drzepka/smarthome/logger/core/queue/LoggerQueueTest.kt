package dev.drzepka.smarthome.logger.core.queue

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.time.Duration

internal class LoggerQueueTest {

    @Test
    fun `should process queue`() = runBlocking {
        val queue = LoggerQueue<String>(3, Duration.ofMinutes(5))
        queue.enqueue("first")
        queue.enqueue("second")
        queue.enqueue("third")
        queue.enqueue("fourth")

        then(queue.size()).isEqualTo(4)

        val batch = queue.getBatch()
        queue.removeBatch(batch)

        val items = ArrayList(batch.items)
        then(items[0].content).isEqualTo("first")
        then(items[1].content).isEqualTo("second")
        then(items[2].content).isEqualTo("third")
        then(queue.size()).isEqualTo(1)

        Unit
    }

    @Test
    fun `should remove oldest elements when queue is full`() = runBlocking {
        val queue = LoggerQueue<String>(2, Duration.ofMinutes(5), 3)
        queue.enqueue("first")
        queue.enqueue("second")
        queue.enqueue("third")
        queue.enqueue("fourth")

        then(queue.size()).isEqualTo(3)

        val batch = queue.getBatch()
        val list = batch.items.map { it.content }

        then(list[0]).isEqualTo("second")
        then(list[1]).isEqualTo("third")

        Unit
    }

    @Test
    fun `should not process expired elements`() = runBlocking {
        val queue = LoggerQueue<String>(1, Duration.ofMillis(300))
        queue.enqueue("first")
        Thread.sleep(300)
        queue.enqueue("second")

        val batch = queue.getBatch()
        val processed = batch.items.first().content
        then(processed).isEqualTo("second")

        Unit
    }
}