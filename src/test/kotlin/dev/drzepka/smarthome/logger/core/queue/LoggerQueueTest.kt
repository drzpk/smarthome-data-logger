package dev.drzepka.smarthome.logger.core.queue

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.time.Duration

internal class LoggerQueueTest {

    @Test
    fun `should process queue - status OK`() = runBlocking {
        val queue = LoggerQueue<String>(3, Duration.ofMinutes(5))
        queue.enqueue("first")
        queue.enqueue("second")
        queue.enqueue("third")
        queue.enqueue("fourth")

        then(queue.size()).isEqualTo(4)

        val list = ArrayList<String>()
        queue.processQueue {
            list.addAll(it.map { item -> item.content })
            ProcessingResult(false, ProcessingResult.Status.OK)
        }

        then(list[0]).isEqualTo("first")
        then(list[1]).isEqualTo("second")
        then(list[2]).isEqualTo("third")
        then(queue.size()).isEqualTo(1)

        Unit
    }

    @Test
    fun `should process queue - status SERVER_UNAVAILABLE`() = runBlocking {
        val queue = LoggerQueue<String>(2, Duration.ofMinutes(5))
        queue.enqueue("first")
        queue.enqueue("second")
        queue.enqueue("third")
        queue.enqueue("fourth")

        then(queue.size()).isEqualTo(4)

        queue.processQueue {
            ProcessingResult(false, ProcessingResult.Status.SERVER_UNAVAILABLE)
        }

        then(queue.size()).isEqualTo(4)

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

        val list = ArrayList<String>()
        queue.processQueue {
            list.addAll(it.map { item -> item.content })
            ProcessingResult(false, ProcessingResult.Status.OK)
        }

        then(list[0]).isEqualTo("second")
        then(list[1]).isEqualTo("third")

        Unit
    }

    @Test
    fun `should note process expired elements`() = runBlocking {
        val queue = LoggerQueue<String>(1, Duration.ofMillis(300))
        queue.enqueue("first")
        Thread.sleep(300)
        queue.enqueue("second")

        var processed: String? = null
        queue.processQueue {
            processed = it.first().content
            ProcessingResult(false, ProcessingResult.Status.OK)
        }

        then(processed).isEqualTo("second")

        Unit
    }

    @Test
    fun `should continue processing`() = runBlocking {
        val queue = LoggerQueue<Int>(1, Duration.ofMinutes(5))
        queue.enqueue(1)
        queue.enqueue(2)
        queue.enqueue(3)

        val processed = ArrayList<Int>()
        var invocations = 0
        queue.processQueue {
            processed.addAll(it.map { item -> item.content })
            ProcessingResult((invocations++) == 0, ProcessingResult.Status.OK)
        }

        then(invocations).isEqualTo(2)
        then(processed).containsExactly(1, 2)

        Unit
    }

    @Test
    fun `should not continue processing on server unavailable error`() = runBlocking {
        val queue = LoggerQueue<Int>(1, Duration.ofMinutes(5))
        queue.enqueue(1)
        queue.enqueue(2)
        queue.enqueue(3)

        val processed = ArrayList<Int>()
        queue.processQueue {
            processed.addAll(it.map { item -> item.content })
            ProcessingResult(true, ProcessingResult.Status.SERVER_UNAVAILABLE)
        }

        then(processed).containsExactly(1)
        then(queue.size()).isEqualTo(3)

        Unit
    }
}