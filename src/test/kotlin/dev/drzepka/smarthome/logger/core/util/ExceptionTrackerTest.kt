package dev.drzepka.smarthome.logger.core.util

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

internal class ErrorTrackerTest {
    @Test
    fun `should track exception - different messages`() {
        val cause1 = NullPointerException("null1")
        val exception1 = IllegalArgumentException("message1", cause1)
        val cause2 = NullPointerException("null2")
        val exception2 = IllegalArgumentException("message1", cause2)

        val tracker = ErrorTracker("test")

        tracker.recordFailure(exception1)
        tracker.recordFailure(exception1)
        then(tracker.consecutiveErrors).isEqualTo(2)
        then(tracker.exceptionChanged).isFalse

        tracker.recordFailure(exception2)
        then(tracker.consecutiveErrors).isEqualTo(3)
        then(tracker.exceptionChanged).isTrue

        tracker.recordSuccess()
        then(tracker.consecutiveErrors).isEqualTo(0)
        then(tracker.exceptionChanged).isFalse
    }

    @Test
    fun `should track exception - different classes`() {
        val cause1 = NullPointerException("cause")
        val exception1 = IllegalArgumentException("argument", cause1)
        val cause2 = IllegalStateException("cause")
        val exception2 = IllegalArgumentException("argument", cause2)

        val tracker = ErrorTracker("test")

        tracker.recordFailure(exception1)
        tracker.recordFailure(exception1)
        then(tracker.consecutiveErrors).isEqualTo(2)
        then(tracker.exceptionChanged).isFalse

        tracker.recordFailure(exception2)
        then(tracker.consecutiveErrors).isEqualTo(3)
        then(tracker.exceptionChanged).isTrue
    }
}
