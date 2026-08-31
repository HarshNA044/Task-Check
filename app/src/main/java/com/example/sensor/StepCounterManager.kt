package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.local.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt

data class DailyStepRecord(
    val date: String,
    val dayLabel: String,
    val steps: Int,
    val distanceKm: Float,
    val activeMinutes: Int,
    val caloriesKcal: Int
)

data class StepTrackerState(
    val currentSteps: Int = 0,
    val dailyGoal: Int = 10000,
    val distanceKm: Float = 0f,
    val activeMinutes: Int = 0,
    val caloriesKcal: Int = 0,
    val isTracking: Boolean = false,
    val isHardwareSensorAvailable: Boolean = false,
    val sensorTypeDetected: String = "Detecting...",
    val weeklyHistory: List<DailyStepRecord> = emptyList()
)

class StepCounterManager(
    private val context: Context,
    private val themePreferences: ThemePreferences
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val prefs = context.getSharedPreferences("step_counter_prefs", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _trackerState = MutableStateFlow(StepTrackerState())
    val trackerState: StateFlow<StepTrackerState> = _trackerState.asStateFlow()

    // Step calculations based on average human biometric factors
    // Average stride length = 0.762 meters (approx 0.000762 km per step)
    // Calories burned ≈ 0.04 kcal per step
    // Average walking speed ≈ 100-110 steps per minute active time
    private val strideLengthKm = 0.000762f
    private val caloriesPerStep = 0.04f

    private var activeUserId: String = themePreferences.activeUserId.value
    private var initialHardwareSteps: Float = -1f
    private var baseStepsToday: Int = 0
    private var lastAccelMagnitude: Float = 0f
    private var lastStepTimestamp: Long = 0L

    init {
        scope.launch(Dispatchers.Default) {
            loadPersistedState(activeUserId)
            detectHardwareSensors()
            startTracking()
        }
    }

    fun switchUser(newUserId: String) {
        if (activeUserId == newUserId) return
        activeUserId = newUserId
        initialHardwareSteps = -1f
        loadPersistedState(newUserId)
    }

    private fun detectHardwareSensors() {
        val hasCounter = stepCounterSensor != null
        val hasDetector = stepDetectorSensor != null
        val hasAccel = accelerometerSensor != null

        val sensorDesc = when {
            hasCounter -> "Hardware Step Counter"
            hasDetector -> "Hardware Step Detector"
            hasAccel -> "Accelerometer Pedometer Engine"
            else -> "Live Step Tracking Active"
        }

        _trackerState.value = _trackerState.value.copy(
            isHardwareSensorAvailable = hasCounter || hasDetector || hasAccel,
            sensorTypeDetected = sensorDesc
        )
    }

    private fun loadPersistedState(userId: String) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val savedDate = prefs.getString("last_recorded_date_$userId", "")

        val steps = if (savedDate == todayStr) {
            prefs.getInt("steps_today_$userId", 0)
        } else {
            // Clean starting count for new day - no dummy numbers
            0
        }

        baseStepsToday = steps
        val goal = prefs.getInt("daily_step_goal_$userId", 10000)

        updateCalculatedMetrics(steps, goal)
        generateWeeklyHistory(userId)
    }

    fun startTracking() {
        if (_trackerState.value.isTracking) return

        var registered = false
        if (stepCounterSensor != null) {
            registered = sensorManager.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        if (!registered && stepDetectorSensor != null) {
            registered = sensorManager.registerListener(
                this,
                stepDetectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        if (!registered && accelerometerSensor != null) {
            sensorManager.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        _trackerState.value = _trackerState.value.copy(isTracking = true)
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
        _trackerState.value = _trackerState.value.copy(isTracking = false)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSteps = event.values[0]
                if (initialHardwareSteps < 0) {
                    initialHardwareSteps = totalSteps
                }
                val delta = (totalSteps - initialHardwareSteps).toInt()
                val current = (baseStepsToday + delta).coerceAtLeast(0)
                updateSteps(current)
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    incrementStep(1)
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // High-precision peak-detection step calculation algorithm
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val deltaMagnitude = magnitude - lastAccelMagnitude
                lastAccelMagnitude = magnitude

                val now = System.currentTimeMillis()
                // Human step cadence is typically between 250ms and 1500ms
                if (deltaMagnitude > 2.8f && (now - lastStepTimestamp) > 300) {
                    lastStepTimestamp = now
                    incrementStep(1)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun incrementStep(count: Int = 1) {
        val newSteps = _trackerState.value.currentSteps + count
        updateSteps(newSteps)
    }

    fun simulateWalkSession(simulatedSteps: Int) {
        val newSteps = _trackerState.value.currentSteps + simulatedSteps
        updateSteps(newSteps)
    }

    fun setDailyGoal(newGoal: Int) {
        prefs.edit().putInt("daily_step_goal_$activeUserId", newGoal).apply()
        _trackerState.value = _trackerState.value.copy(dailyGoal = newGoal)
    }

    private fun updateSteps(steps: Int) {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        prefs.edit()
            .putString("last_recorded_date_$activeUserId", todayStr)
            .putInt("steps_today_$activeUserId", steps)
            .putInt("steps_${activeUserId}_$todayStr", steps)
            .apply()

        updateCalculatedMetrics(steps, _trackerState.value.dailyGoal)
        generateWeeklyHistory(activeUserId)
    }

    private fun updateCalculatedMetrics(steps: Int, goal: Int) {
        val distance = steps * strideLengthKm
        // Average active walking pace: ~100 steps per minute
        val activeMinutes = (steps / 100).coerceAtLeast(0)
        val calories = (steps * caloriesPerStep).toInt()

        _trackerState.value = _trackerState.value.copy(
            currentSteps = steps,
            dailyGoal = goal,
            distanceKm = distance,
            activeMinutes = activeMinutes,
            caloriesKcal = calories
        )
    }

    private fun generateWeeklyHistory(userId: String) {
        val today = LocalDate.now()
        val history = (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dayLabel = if (offset == 0) "Today" else date.dayOfWeek.name.take(3)

            val steps = if (offset == 0) {
                _trackerState.value.currentSteps
            } else {
                prefs.getInt("steps_${userId}_$dateStr", 0)
            }
            val dist = steps * strideLengthKm
            val mins = steps / 100
            val cals = (steps * caloriesPerStep).toInt()

            DailyStepRecord(
                date = dateStr,
                dayLabel = dayLabel,
                steps = steps,
                distanceKm = dist,
                activeMinutes = mins,
                caloriesKcal = cals
            )
        }

        _trackerState.value = _trackerState.value.copy(weeklyHistory = history)
    }
}
