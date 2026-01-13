package io.designtoswiftui.cookmode.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import io.designtoswiftui.cookmode.MainActivity
import io.designtoswiftui.cookmode.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TimerState(
    val isRunning: Boolean = false,
    val secondsRemaining: Int = 0,
    val totalSeconds: Int = 0,
    val stepInstruction: String = "",
    val isComplete: Boolean = false
)

class TimerService : Service() {

    private val binder = TimerBinder()
    private var countDownTimer: CountDownTimer? = null

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    companion object {
        const val CHANNEL_ID_TIMER = "cookmode_timer_channel"
        const val CHANNEL_ID_ALARM = "cookmode_alarm_channel"
        const val NOTIFICATION_ID_TIMER = 1001
        const val NOTIFICATION_ID_ALARM = 1002

        const val ACTION_START = "io.designtoswiftui.cookmode.timer.START"
        const val ACTION_PAUSE = "io.designtoswiftui.cookmode.timer.PAUSE"
        const val ACTION_RESUME = "io.designtoswiftui.cookmode.timer.RESUME"
        const val ACTION_STOP = "io.designtoswiftui.cookmode.timer.STOP"

        const val EXTRA_SECONDS = "extra_seconds"
        const val EXTRA_STEP_INSTRUCTION = "extra_step_instruction"

        fun createStartIntent(
            context: Context,
            seconds: Int,
            stepInstruction: String
        ): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SECONDS, seconds)
                putExtra(EXTRA_STEP_INSTRUCTION, stepInstruction)
            }
        }

        fun createPauseIntent(context: Context): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = ACTION_PAUSE
            }
        }

        fun createResumeIntent(context: Context): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = ACTION_RESUME
            }
        }

        fun createStopIntent(context: Context): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val seconds = intent.getIntExtra(EXTRA_SECONDS, 0)
                val instruction = intent.getStringExtra(EXTRA_STEP_INSTRUCTION) ?: ""
                startTimer(seconds, instruction)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Timer ongoing channel (silent)
            val timerChannel = NotificationChannel(
                CHANNEL_ID_TIMER,
                "Timer Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows cooking timer progress"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(timerChannel)

            // Alarm channel (with sound and vibration)
            val alarmChannel = NotificationChannel(
                CHANNEL_ID_ALARM,
                "Timer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when cooking timer completes"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(alarmChannel)
        }
    }

    private fun startTimer(seconds: Int, instruction: String) {
        countDownTimer?.cancel()

        _timerState.value = TimerState(
            isRunning = true,
            secondsRemaining = seconds,
            totalSeconds = seconds,
            stepInstruction = instruction,
            isComplete = false
        )

        val notification = createTimerNotification(seconds, instruction)
        startForeground(NOTIFICATION_ID_TIMER, notification)

        countDownTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = (millisUntilFinished / 1000).toInt()
                _timerState.value = _timerState.value.copy(
                    secondsRemaining = remaining
                )
                updateNotification(remaining, instruction)
            }

            override fun onFinish() {
                _timerState.value = _timerState.value.copy(
                    isRunning = false,
                    secondsRemaining = 0,
                    isComplete = true
                )
                onTimerComplete(instruction)
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
        updateNotification(
            _timerState.value.secondsRemaining,
            _timerState.value.stepInstruction,
            isPaused = true
        )
    }

    private fun resumeTimer() {
        val remaining = _timerState.value.secondsRemaining
        val instruction = _timerState.value.stepInstruction
        if (remaining > 0) {
            _timerState.value = _timerState.value.copy(isRunning = true)

            countDownTimer = object : CountDownTimer(remaining * 1000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    val secs = (millisUntilFinished / 1000).toInt()
                    _timerState.value = _timerState.value.copy(secondsRemaining = secs)
                    updateNotification(secs, instruction)
                }

                override fun onFinish() {
                    _timerState.value = _timerState.value.copy(
                        isRunning = false,
                        secondsRemaining = 0,
                        isComplete = true
                    )
                    onTimerComplete(instruction)
                }
            }.start()
        }
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        _timerState.value = TimerState()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        val totalSeconds = _timerState.value.totalSeconds
        val instruction = _timerState.value.stepInstruction
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            secondsRemaining = totalSeconds,
            isComplete = false
        )
        updateNotification(totalSeconds, instruction, isPaused = true)
    }

    fun clearCompletionState() {
        _timerState.value = _timerState.value.copy(isComplete = false)
        // Cancel alarm notification
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID_ALARM)
    }

    private fun createTimerNotification(seconds: Int, instruction: String): Notification {
        val pendingIntent = createContentPendingIntent()
        val pauseIntent = createActionPendingIntent(ACTION_PAUSE)
        val stopIntent = createActionPendingIntent(ACTION_STOP)

        return NotificationCompat.Builder(this, CHANNEL_ID_TIMER)
            .setContentTitle("Timer: ${formatTime(seconds)}")
            .setContentText(instruction.take(50))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "Pause", pauseIntent)
            .addAction(0, "Stop", stopIntent)
            .setProgress(100, 100, false)
            .build()
    }

    private fun updateNotification(seconds: Int, instruction: String, isPaused: Boolean = false) {
        val pendingIntent = createContentPendingIntent()
        val totalSeconds = _timerState.value.totalSeconds
        val progress = if (totalSeconds > 0) {
            ((seconds.toFloat() / totalSeconds) * 100).toInt()
        } else 0

        val builder = NotificationCompat.Builder(this, CHANNEL_ID_TIMER)
            .setContentTitle(if (isPaused) "Paused: ${formatTime(seconds)}" else "Timer: ${formatTime(seconds)}")
            .setContentText(instruction.take(50))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setProgress(100, progress, false)

        if (isPaused) {
            val resumeIntent = createActionPendingIntent(ACTION_RESUME)
            val stopIntent = createActionPendingIntent(ACTION_STOP)
            builder.addAction(0, "Resume", resumeIntent)
            builder.addAction(0, "Stop", stopIntent)
        } else {
            val pauseIntent = createActionPendingIntent(ACTION_PAUSE)
            val stopIntent = createActionPendingIntent(ACTION_STOP)
            builder.addAction(0, "Pause", pauseIntent)
            builder.addAction(0, "Stop", stopIntent)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_TIMER, builder.build())
    }

    private fun onTimerComplete(instruction: String) {
        // Show alarm notification
        showAlarmNotification(instruction)

        // Vibrate
        vibrate()

        // Stop foreground but keep service alive for binding
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun showAlarmNotification(instruction: String) {
        val pendingIntent = createContentPendingIntent()
        val dismissIntent = createActionPendingIntent(ACTION_STOP)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ALARM)
            .setContentTitle("Timer Complete!")
            .setContentText(instruction.take(50))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "Dismiss", dismissIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_ALARM, notification)
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
        }
    }

    private fun createContentPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply {
            this.action = action
        }
        val requestCode = when (action) {
            ACTION_PAUSE -> 1
            ACTION_RESUME -> 2
            ACTION_STOP -> 3
            else -> 0
        }
        return PendingIntent.getService(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatTime(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
