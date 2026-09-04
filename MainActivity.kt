package com.gymlock.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TimePicker
import android.widget.ToggleButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    // Calendar day constants, ordered Sat->Fri to match the Arabic week the person is used to.
    private val weekDays = listOf(
        Calendar.SATURDAY to "سبت",
        Calendar.SUNDAY to "حد",
        Calendar.MONDAY to "إتنين",
        Calendar.TUESDAY to "تلات",
        Calendar.WEDNESDAY to "أربع",
        Calendar.THURSDAY to "خميس",
        Calendar.FRIDAY to "جمعة"
    )

    private val MAX_RECOMMENDED_DAYS = 6 // gentle heads-up only, never blocks selection
    private val chipMap = mutableMapOf<Int, ToggleButton>()
    private lateinit var daysGrid: FlexboxLayout
    private lateinit var timePicker: TimePicker

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op, user's choice either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        daysGrid = findViewById(R.id.daysGrid)
        timePicker = findViewById(R.id.timePicker)
        timePicker.setIs24HourView(true)

        buildDayChips()
        restoreSavedSettings()
        requestNotificationPermissionIfNeeded()
        setupOverlayPermissionButton()

        findViewById<android.widget.Button>(R.id.btnSave).setOnClickListener { saveAndSchedule() }
        findViewById<android.widget.Button>(R.id.btnViewPlan).setOnClickListener {
            startActivity(Intent(this, PlanActivity::class.java))
        }

        // First run: make sure the fixed 1 PM daily prompt is armed regardless of gym-day setup.
        AlarmScheduler.scheduleDailyPrompt(this)
    }

    private fun buildDayChips() {
        weekDays.forEach { (dayConst, label) ->
            val chip = layoutInflater.inflate(R.layout.item_day_chip, daysGrid, false) as ToggleButton
            chip.text = label
            chip.textOn = label
            chip.textOff = label
            chip.setOnClickListener {
                val selectedCount = chipMap.values.count { it.isChecked }
                if (chip.isChecked && selectedCount == 7) {
                    Toast.makeText(this, "اخترت كل أيام الأسبوع — حاول تسيب يوم راحة على الأقل", Toast.LENGTH_LONG).show()
                }
            }
            chipMap[dayConst] = chip
            daysGrid.addView(chip)
        }
    }

    private fun restoreSavedSettings() {
        val savedDays = Prefs.getDays(this)
        savedDays.forEach { day -> chipMap[day]?.isChecked = true }
        timePicker.hour = Prefs.getHour(this)
        timePicker.minute = Prefs.getMinute(this)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setupOverlayPermissionButton() {
        findViewById<android.widget.Button>(R.id.btnGrantOverlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "الإذن متاح بالفعل ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAndSchedule() {
        val selectedDays = chipMap.filterValues { it.isChecked }.keys
        if (selectedDays.isEmpty()) {
            findViewById<android.widget.TextView>(R.id.saveMsg).text = "اختار الأيام التلاتة الأول"
            return
        }

        Prefs.saveDays(this, selectedDays)
        Prefs.saveTime(this, timePicker.hour, timePicker.minute)

        // Ask for exact-alarm scheduling on Android 12+ if not yet granted.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(ALARM_SERVICE) as android.app.AlarmManager
            if (!am.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
        }

        AlarmScheduler.rescheduleAll(this)
        findViewById<android.widget.TextView>(R.id.saveMsg).text =
            "تم الحفظ ✓ — هيتقفل الموبايل في ${selectedDays.size} أيام لحد ما ترفع صورة الجيم. دوس \"اعرض خطة الأسبوع\" تحت عشان تشوف التمارين."
    }
}
