package com.gymlock.app

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PlanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan)

        val selectedDays = Prefs.getDays(this)
        val plan = WorkoutPlanProvider.generatePlan(selectedDays)

        findViewById<TextView>(R.id.planSubtitle).text =
            "${plan.size} أيام تمرين في الأسبوع — الخطة اتبنت على الأيام اللي اخترتها بالظبط"

        val container = findViewById<LinearLayout>(R.id.planContainer)
        container.removeAllViews()

        plan.forEach { dayPlan ->
            val card = layoutInflater.inflate(R.layout.item_day_plan, container, false)
            card.findViewById<TextView>(R.id.dayTitle).text = "${dayPlan.dayLabel} — ${dayPlan.focus}"
            card.findViewById<TextView>(R.id.daySubtitle).text = dayPlan.duration
            card.findViewById<TextView>(R.id.dayTip).text = dayPlan.tip

            val exContainer = card.findViewById<LinearLayout>(R.id.exerciseContainer)
            dayPlan.exercises.forEach { ex ->
                val row = layoutInflater.inflate(R.layout.item_exercise_row, exContainer, false)
                row.findViewById<TextView>(R.id.exName).text = ex.name
                row.findViewById<TextView>(R.id.exSets).text = ex.sets
                exContainer.addView(row)
            }

            container.addView(card)
        }
    }
}
