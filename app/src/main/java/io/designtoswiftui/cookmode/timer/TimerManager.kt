package io.designtoswiftui.cookmode.timer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var timerService: TimerService? = null
    private var isBound = false

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _isServiceBound = MutableStateFlow(false)
    val isServiceBound: StateFlow<Boolean> = _isServiceBound.asStateFlow()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? TimerService.TimerBinder
            timerService = binder?.getService()
            isBound = true
            _isServiceBound.value = true

            // Observe timer state from service
            timerService?.let { svc ->
                // Note: In a real implementation, we'd collect the service's StateFlow
                // For now, the ViewModel will observe the service directly after binding
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            isBound = false
            _isServiceBound.value = false
        }
    }

    fun bindService() {
        if (!isBound) {
            val intent = Intent(context, TimerService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService() {
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
            _isServiceBound.value = false
            timerService = null
        }
    }

    fun startTimer(seconds: Int, stepInstruction: String) {
        if (!hasNotificationPermission()) {
            // Timer will still work, but no notification
            // The UI should prompt for permission
        }

        val intent = TimerService.createStartIntent(context, seconds, stepInstruction)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        // Bind to get state updates
        bindService()
    }

    fun pauseTimer() {
        val intent = TimerService.createPauseIntent(context)
        context.startService(intent)
    }

    fun resumeTimer() {
        val intent = TimerService.createResumeIntent(context)
        context.startService(intent)
    }

    fun stopTimer() {
        val intent = TimerService.createStopIntent(context)
        context.startService(intent)
    }

    fun resetTimer() {
        timerService?.resetTimer()
    }

    fun clearCompletionState() {
        timerService?.clearCompletionState()
    }

    fun getService(): TimerService? = timerService

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required on older versions
        }
    }

    fun needsNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()
    }
}
