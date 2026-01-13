package io.designtoswiftui.cookmode.ui.cooking

import io.designtoswiftui.cookmode.models.Step

data class CookingState(
    val recipeTitle: String = "",
    val steps: List<Step> = emptyList(),
    val currentStepIndex: Int = 0,
    val isTimerRunning: Boolean = false,
    val timerSecondsRemaining: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val currentStep: Step?
        get() = steps.getOrNull(currentStepIndex)

    val isFirstStep: Boolean
        get() = currentStepIndex == 0

    val isLastStep: Boolean
        get() = steps.isEmpty() || currentStepIndex == steps.lastIndex

    val progress: Float
        get() = if (steps.isEmpty()) 0f else (currentStepIndex + 1).toFloat() / steps.size

    val stepProgress: String
        get() = if (steps.isEmpty()) "0 of 0" else "${currentStepIndex + 1} of ${steps.size}"

    val currentStepTimerSeconds: Int?
        get() = currentStep?.timerSeconds

    val hasTimer: Boolean
        get() = currentStepTimerSeconds != null

    val timerDisplay: String
        get() {
            val seconds = timerSecondsRemaining ?: currentStepTimerSeconds ?: return ""
            val minutes = seconds / 60
            val secs = seconds % 60
            return "%d:%02d".format(minutes, secs)
        }
}
