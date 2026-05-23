package dev.drzepka.smarthome.logger.core.scheduler

import dev.drzepka.smarthome.logger.core.config.SchedulerProperties
import dev.drzepka.smarthome.logger.core.util.ErrorTracker
import dev.drzepka.smarthome.logger.core.util.Logger
import dev.drzepka.smarthome.logger.core.util.suspendRunCatching
import kotlinx.coroutines.*
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.ceil

class TaskScheduler(threadPoolSize: Int = 8, private val schedulerProperties: SchedulerProperties) {
    private val log by Logger()
    private val activeTasks = ConcurrentHashMap.newKeySet<String>()
    private val trackers = ConcurrentHashMap<String, ErrorTracker>()
    private val scope = CoroutineScope(Executors.newFixedThreadPool(threadPoolSize).asCoroutineDispatcher() + SupervisorJob() + createExceptionHandler())

    @Synchronized
    fun schedule(name: String, interval: Duration, task: (suspend () -> Unit)) {
        if (isActive(name))
            throw IllegalArgumentException("Task '$name' already scheduled")

        log.info("Scheduling task '{}'", name)
        activeTasks.add(name)

        val tracker = ErrorTracker(
            name,
            schedulerProperties.errorThreshold,
            schedulerProperties.throttleSkipCount,
            schedulerProperties.backoffFactor,
            schedulerProperties.maxSkipCount
        )
        trackers[name] = tracker

        startTask(name, interval, task, tracker)
    }

    @Synchronized
    fun cancel(name: String) {
        log.info("Cancelling task '{}'", name)
        val removed = activeTasks.remove(name)
        if (!removed)
            log.warn("No scheduled task '{}' was found", name)
        trackers.remove(name)
    }

    private fun createExceptionHandler(): CoroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        log.error("Uncaught task scheduler exception", throwable)
    }

    private fun startTask(name: String, interval: Duration, task: (suspend () -> Unit), tracker: ErrorTracker) {
        scope.launch {
            delay(getInitialDelay(interval))

            var nextPlannedExecution = Instant.now()
            while (isActive(name)) {
                log.info("Executing task '{}'", name)

                if (!tracker.shouldSkip()) {
                    tracker.suspendRunCatching(log, "Error while executing task '$name'") {
                        task.invoke()
                    }
                }

                nextPlannedExecution = nextPlannedExecution.plus(interval)
                val now = Instant.now()
                var millis = nextPlannedExecution.toEpochMilli() - now.toEpochMilli()

                if (millis < 1) {
                    val multiplicand = ceil(abs(millis) / interval.toMillis().toFloat()).toLong()
                    log.warn(
                        "Execution of task '{}' took longer than its interval, skipping, next {} intervals",
                        name, multiplicand
                    )

                    nextPlannedExecution = nextPlannedExecution.plus(interval.multipliedBy(multiplicand))
                    millis = nextPlannedExecution.toEpochMilli() - now.toEpochMilli()
                }

                delay(millis)
            }
        }
    }

    private fun isActive(name: String): Boolean = activeTasks.contains(name)

    private fun getInitialDelay(interval: Duration): Long {
        val intervalMillis = interval.toMillis()
        val now = Instant.now().toEpochMilli()
        return intervalMillis - (now % intervalMillis)
    }
}