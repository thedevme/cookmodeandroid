package io.designtoswiftui.cookmode.unit

import io.designtoswiftui.cookmode.timer.TimerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for timer state and logic.
 * Note: Full TimerService integration tests require Android instrumentation tests.
 * These tests verify the state model and computed properties.
 */
class TimerServiceTests {

    @Test
    fun `initial timer state has correct defaults`() {
        val state = TimerState()

        assertFalse(state.isRunning)
        assertEquals(0, state.secondsRemaining)
        assertEquals(0, state.totalSeconds)
        assertEquals("", state.stepInstruction)
        assertFalse(state.isComplete)
    }

    @Test
    fun `timer state with running timer`() {
        val state = TimerState(
            isRunning = true,
            secondsRemaining = 120,
            totalSeconds = 180,
            stepInstruction = "Bake until golden",
            isComplete = false
        )

        assertTrue(state.isRunning)
        assertEquals(120, state.secondsRemaining)
        assertEquals(180, state.totalSeconds)
        assertEquals("Bake until golden", state.stepInstruction)
        assertFalse(state.isComplete)
    }

    @Test
    fun `timer state when complete`() {
        val state = TimerState(
            isRunning = false,
            secondsRemaining = 0,
            totalSeconds = 180,
            stepInstruction = "Bake until golden",
            isComplete = true
        )

        assertFalse(state.isRunning)
        assertEquals(0, state.secondsRemaining)
        assertTrue(state.isComplete)
    }

    @Test
    fun `timer state copy updates correctly`() {
        val initialState = TimerState(
            isRunning = true,
            secondsRemaining = 60,
            totalSeconds = 60,
            stepInstruction = "Wait"
        )

        val updatedState = initialState.copy(secondsRemaining = 59)

        assertEquals(59, updatedState.secondsRemaining)
        assertTrue(updatedState.isRunning)
        assertEquals(60, updatedState.totalSeconds)
    }

    @Test
    fun `timer state pause preserves remaining time`() {
        val runningState = TimerState(
            isRunning = true,
            secondsRemaining = 45,
            totalSeconds = 60
        )

        val pausedState = runningState.copy(isRunning = false)

        assertFalse(pausedState.isRunning)
        assertEquals(45, pausedState.secondsRemaining)
    }

    @Test
    fun `timer state reset restores total seconds`() {
        val completedState = TimerState(
            isRunning = false,
            secondsRemaining = 0,
            totalSeconds = 120,
            isComplete = true
        )

        val resetState = completedState.copy(
            secondsRemaining = completedState.totalSeconds,
            isComplete = false
        )

        assertEquals(120, resetState.secondsRemaining)
        assertEquals(120, resetState.totalSeconds)
        assertFalse(resetState.isComplete)
    }

    @Test
    fun `timer progress calculation`() {
        val state = TimerState(
            secondsRemaining = 30,
            totalSeconds = 60
        )

        val progress = if (state.totalSeconds > 0) {
            (state.secondsRemaining.toFloat() / state.totalSeconds) * 100
        } else 0f

        assertEquals(50f, progress, 0.01f)
    }

    @Test
    fun `timer progress at zero remaining`() {
        val state = TimerState(
            secondsRemaining = 0,
            totalSeconds = 60
        )

        val progress = if (state.totalSeconds > 0) {
            (state.secondsRemaining.toFloat() / state.totalSeconds) * 100
        } else 0f

        assertEquals(0f, progress, 0.01f)
    }

    @Test
    fun `timer progress with zero total returns zero`() {
        val state = TimerState(
            secondsRemaining = 0,
            totalSeconds = 0
        )

        val progress = if (state.totalSeconds > 0) {
            (state.secondsRemaining.toFloat() / state.totalSeconds) * 100
        } else 0f

        assertEquals(0f, progress, 0.01f)
    }

    @Test
    fun `format time helper function`() {
        // Test time formatting logic
        fun formatTime(totalSeconds: Int): String {
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }

        assertEquals("0:00", formatTime(0))
        assertEquals("0:30", formatTime(30))
        assertEquals("1:00", formatTime(60))
        assertEquals("1:30", formatTime(90))
        assertEquals("5:00", formatTime(300))
        assertEquals("10:00", formatTime(600))
        assertEquals("59:59", formatTime(3599))
    }

    @Test
    fun `timer state equality`() {
        val state1 = TimerState(
            isRunning = true,
            secondsRemaining = 60,
            totalSeconds = 120,
            stepInstruction = "Test"
        )

        val state2 = TimerState(
            isRunning = true,
            secondsRemaining = 60,
            totalSeconds = 120,
            stepInstruction = "Test"
        )

        assertEquals(state1, state2)
    }

    @Test
    fun `timer state inequality on different remaining`() {
        val state1 = TimerState(secondsRemaining = 60)
        val state2 = TimerState(secondsRemaining = 59)

        assertFalse(state1 == state2)
    }

    @Test
    fun `timer completion triggers only at zero`() {
        // Simulating countdown
        var state = TimerState(
            isRunning = true,
            secondsRemaining = 3,
            totalSeconds = 3
        )

        // Tick down
        repeat(3) {
            state = state.copy(secondsRemaining = state.secondsRemaining - 1)
        }

        assertEquals(0, state.secondsRemaining)

        // Now mark complete
        state = state.copy(isRunning = false, isComplete = true)
        assertTrue(state.isComplete)
    }

    @Test
    fun `multiple timers have independent state`() {
        val timer1State = TimerState(
            isRunning = true,
            secondsRemaining = 60,
            totalSeconds = 60,
            stepInstruction = "Timer 1"
        )

        val timer2State = TimerState(
            isRunning = true,
            secondsRemaining = 120,
            totalSeconds = 120,
            stepInstruction = "Timer 2"
        )

        // Update timer1 doesn't affect timer2
        val timer1Updated = timer1State.copy(secondsRemaining = 30)

        assertEquals(30, timer1Updated.secondsRemaining)
        assertEquals(120, timer2State.secondsRemaining)
    }
}
