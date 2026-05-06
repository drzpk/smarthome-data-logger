package dev.drzepka.smarthome.logger.core.scheduler

import dev.drzepka.smarthome.logger.core.config.PropertiesConfigPropertySource
import dev.drzepka.smarthome.logger.core.config.SchedulerProperties
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import java.time.Duration

class TaskSchedulerTest {

    @Test
    fun `should schedule task`() {
        val scheduler = scheduler()

        var counter = 0
        scheduler.schedule("name", Duration.ofMillis(200)) {
            counter++
        }

        Thread.sleep(650)
        then(counter).isBetween(3, 5)
    }

    @Test
    fun `should cancel scheduled task`() {
        val scheduler = scheduler()

        var counter = 0
        scheduler.schedule("name", Duration.ofMillis(100)) {
            counter++
        }

        Thread.sleep(250)
        scheduler.cancel("name")
        val saved = counter

        then(saved).isGreaterThan(0)
        Thread.sleep(200)
        then(counter).isEqualTo(saved)
    }

    @Test
    fun `should throw exception on scheduling multiple tasks with the same name`() {
        val scheduler = scheduler()

        scheduler.schedule("name", Duration.ofSeconds(1)) {}

        assertThatIllegalArgumentException().isThrownBy {
            scheduler.schedule("name", Duration.ofMinutes(1)) {}
        }.withMessage("Task 'name' already scheduled")
    }

    @Test
    fun `should continue working if exception occurs in the handler function`() {
        val scheduler = scheduler(errorThreshold = 999999)

        var counter = 0
        scheduler.schedule("name", Duration.ofMillis(1)) {
            counter++
            throw Exception("Test exception")
        }

        Thread.sleep(50)
        scheduler.cancel("name")

        then(counter).isGreaterThan(1)
    }

    @Test
    fun `should throttle task after error threshold is reached`() {
        val scheduler = scheduler(errorThreshold = 2, throttleSkipCount = 10)

        var counter = 0
        scheduler.schedule("name", Duration.ofMillis(10)) {
            counter++
            throw Exception("always fails")
        }

        Thread.sleep(150)
        scheduler.cancel("name")

        then(counter).isEqualTo(2)
    }

    private fun scheduler(errorThreshold: Int = 999999, throttleSkipCount: Int = 0) = TaskScheduler(
        1,
        SchedulerProperties(
            PropertiesConfigPropertySource(
                "scheduler.errorThreshold=$errorThreshold\nscheduler.throttleSkipCount=$throttleSkipCount"
            )
        )
    )
}
