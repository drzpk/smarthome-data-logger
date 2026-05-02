package dev.drzepka.smarthome.logger.core.queue

import dev.drzepka.smarthome.logger.core.model.measurement.Measurement
import dev.drzepka.smarthome.logger.core.model.measurement.TemperatureMeasurement
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Duration

internal class LoggerQueueTest {

    @Test
    fun `should process queue`() = runBlocking {
        val queue = LoggerQueue(3, Duration.ofMinutes(5), 1000)
        val first = measurement("first")
        val second = measurement("second")
        val third = measurement("third")
        val fourth = measurement("fourth")
        queue.enqueue(first)
        queue.enqueue(second)
        queue.enqueue(third)
        queue.enqueue(fourth)

        then(queue.size()).isEqualTo(4)

        val batch = queue.getBatch()
        queue.removeBatch(batch)

        val items = ArrayList(batch.items)
        then(items[0].content).isEqualTo(first)
        then(items[1].content).isEqualTo(second)
        then(items[2].content).isEqualTo(third)
        then(queue.size()).isEqualTo(1)

        Unit
    }

    @Test
    fun `should remove oldest elements when queue is full`() = runBlocking {
        val queue = LoggerQueue(2, Duration.ofMinutes(5), 3)
        queue.enqueue(measurement("first"))
        queue.enqueue(measurement("second"))
        queue.enqueue(measurement("third"))
        queue.enqueue(measurement("fourth"))

        then(queue.size()).isEqualTo(3)

        val batch = queue.getBatch()
        val list = batch.items.map { it.content.mac }

        then(list[0]).isEqualTo("second")
        then(list[1]).isEqualTo("third")

        Unit
    }

    @Test
    fun `should not process expired elements`() = runBlocking {
        val queue = LoggerQueue(1, Duration.ofMillis(300), 1000)
        val first = measurement("first")
        val second = measurement("second")
        queue.enqueue(first)
        delay(350)
        queue.enqueue(second)

        val batch = queue.getBatch()
        val processed = batch.items.first().content
        then(processed).isEqualTo(second)

        Unit
    }

    private fun measurement(mac: String): Measurement =
        TemperatureMeasurement(mac = mac, temperature = BigDecimal.ZERO)
}