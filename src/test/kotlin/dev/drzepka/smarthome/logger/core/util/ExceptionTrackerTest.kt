package dev.drzepka.smarthome.logger.core.util

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

internal class ExceptionTrackerTest {
    @Test
    fun `should track exception - different messages`() {
        val cause1 = NullPointerException("null1")
        val exception1 = IllegalArgumentException("message1", cause1)
        val cause2 = NullPointerException("null2")
        val exception2 = IllegalArgumentException("message1", cause2)

        val tracker = ExceptionTracker("test")

        tracker.setLastException(exception1)
        tracker.setLastException(exception1)
        then(tracker.exceptionCount).isEqualTo(2)
        then(tracker.exceptionChanged).isFalse

        tracker.setLastException(exception2)
        then(tracker.exceptionCount).isEqualTo(1)
        then(tracker.exceptionChanged).isTrue

        tracker.reset()
        then(tracker.exceptionCount).isEqualTo(0)
        then(tracker.exceptionChanged).isFalse
    }

    @Test
    fun `should track exception - different classes`() {
        val cause1 = NullPointerException("cause")
        val exception1 = IllegalArgumentException("argument", cause1)
        val cause2 = IllegalStateException("cause")
        val exception2 = IllegalArgumentException("argument", cause2)

        val tracker = ExceptionTracker("test")

        tracker.setLastException(exception1)
        tracker.setLastException(exception1)
        then(tracker.exceptionCount).isEqualTo(2)
        then(tracker.exceptionChanged).isFalse

        tracker.setLastException(exception2)
        then(tracker.exceptionCount).isEqualTo(1)
        then(tracker.exceptionChanged).isTrue
    }
}
