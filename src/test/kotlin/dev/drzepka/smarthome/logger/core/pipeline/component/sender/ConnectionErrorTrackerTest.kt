package dev.drzepka.smarthome.logger.core.pipeline.component.sender

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test

internal class ConnectionErrorTrackerTest {

    @Test
    fun `should not skip when no errors occurred`() {
        val tracker = tracker()

        then(tracker.shouldSkip()).isFalse
    }

    @Test
    fun `should log errors below threshold`() {
        val tracker = tracker(errorThreshold = 3)

        then(tracker.recordConnectionFailure()).isTrue
        then(tracker.recordConnectionFailure()).isTrue
    }

    @Test
    fun `should suppress logging at threshold and start throttle countdown`() {
        val tracker = tracker(errorThreshold = 3, throttleSkipCount = 4)

        tracker.recordConnectionFailure()
        tracker.recordConnectionFailure()
        val shouldLog = tracker.recordConnectionFailure()

        then(shouldLog).isFalse
        then(tracker.consecutiveErrors).isEqualTo(3)
    }

    @Test
    fun `should suppress logging above threshold`() {
        val tracker = tracker(errorThreshold = 3, throttleSkipCount = 4)

        repeat(3) { tracker.recordConnectionFailure() }
        tracker.shouldSkip() // consume countdown so next call executes
        repeat(4) { tracker.shouldSkip() }

        val shouldLog = tracker.recordConnectionFailure()

        then(shouldLog).isFalse
    }

    @Test
    fun `should skip throttleSkipCount cycles after reaching error threshold`() {
        val tracker = tracker(errorThreshold = 3, throttleSkipCount = 4)

        repeat(3) { tracker.recordConnectionFailure() }

        then(tracker.shouldSkip()).isTrue
        then(tracker.shouldSkip()).isTrue
        then(tracker.shouldSkip()).isTrue
        then(tracker.shouldSkip()).isTrue
        then(tracker.shouldSkip()).isFalse
    }

    @Test
    fun `should skip again after failed retry when throttled`() {
        val tracker = tracker(errorThreshold = 3, throttleSkipCount = 2)

        repeat(3) { tracker.recordConnectionFailure() }
        repeat(2) { tracker.shouldSkip() }  // exhaust first countdown
        tracker.shouldSkip()                // executes (resets countdown)
        tracker.recordConnectionFailure()   // retry failed

        then(tracker.shouldSkip()).isTrue
        then(tracker.shouldSkip()).isTrue
        then(tracker.shouldSkip()).isFalse
    }

    @Test
    fun `should reset state on success`() {
        val tracker = tracker(errorThreshold = 3, throttleSkipCount = 4)

        repeat(3) { tracker.recordConnectionFailure() }
        repeat(4) { tracker.shouldSkip() }
        tracker.shouldSkip() // execute cycle
        tracker.recordSuccess()

        then(tracker.consecutiveErrors).isEqualTo(0)
        then(tracker.shouldSkip()).isFalse
    }

    @Test
    fun `should reset error count on success without throttle`() {
        val tracker = tracker(errorThreshold = 3)

        tracker.recordConnectionFailure()
        tracker.recordConnectionFailure()
        tracker.recordSuccess()

        then(tracker.consecutiveErrors).isEqualTo(0)
        then(tracker.shouldSkip()).isFalse
    }

    @Test
    fun `should allow normal sends after recovery from throttle`() {
        val tracker = tracker(errorThreshold = 3, throttleSkipCount = 2)

        repeat(3) { tracker.recordConnectionFailure() }
        repeat(2) { tracker.shouldSkip() }
        tracker.shouldSkip()   // execute cycle
        tracker.recordSuccess()

        then(tracker.shouldSkip()).isFalse
        then(tracker.shouldSkip()).isFalse
    }

    @Test
    fun `should resume normal error logging after recovery`() {
        val tracker = tracker(errorThreshold = 3, throttleSkipCount = 1)

        repeat(3) { tracker.recordConnectionFailure() }
        tracker.shouldSkip()
        tracker.shouldSkip() // execute cycle
        tracker.recordSuccess()

        then(tracker.recordConnectionFailure()).isTrue
    }

    @Test
    fun `should increase skip count exponentially on each failed retry`() {
        val tracker = tracker(errorThreshold = 3, throttleSkipCount = 2, backoffFactor = 2.0, maxSkipCount = 16)

        repeat(3) { tracker.recordConnectionFailure() }

        then(skipCount(tracker)).isEqualTo(2)
        tracker.recordConnectionFailure()

        then(skipCount(tracker)).isEqualTo(4)
        tracker.recordConnectionFailure()

        then(skipCount(tracker)).isEqualTo(8)
    }

    @Test
    fun `should cap skip count at maxSkipCount`() {
        val tracker = tracker(errorThreshold = 3, throttleSkipCount = 2, backoffFactor = 4.0, maxSkipCount = 6)

        repeat(3) { tracker.recordConnectionFailure() }
        skipCount(tracker)
        tracker.recordConnectionFailure() // 2 * 4 = 8, capped at 6

        then(skipCount(tracker)).isEqualTo(6)
    }

    @Test
    fun `should reset skip count to initial on recovery`() {
        val tracker = tracker(errorThreshold = 3, throttleSkipCount = 2, backoffFactor = 2.0, maxSkipCount = 16)

        repeat(3) { tracker.recordConnectionFailure() }
        skipCount(tracker)
        tracker.recordConnectionFailure() // backoff applied: currentSkipCount = 4
        skipCount(tracker)
        tracker.recordSuccess()

        // after recovery and re-throttle, should start back at throttleSkipCount
        repeat(3) { tracker.recordConnectionFailure() }
        then(skipCount(tracker)).isEqualTo(2)
    }

    private fun tracker(
        errorThreshold: Int = 3,
        throttleSkipCount: Int = 4,
        backoffFactor: Double = 1.0,
        maxSkipCount: Int = throttleSkipCount
    ) = ConnectionErrorTracker("test", errorThreshold, throttleSkipCount, backoffFactor, maxSkipCount)

    private fun skipCount(tracker: ConnectionErrorTracker): Int {
        var count = 0
        while (tracker.shouldSkip()) count++
        return count
    }
}
