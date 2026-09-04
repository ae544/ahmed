package com.gymlock.app

import java.util.Calendar

data class Exercise(val name: String, val sets: String)
data class DayPlan(
    val dayLabel: String,
    val focus: String,
    val duration: String,
    val exercises: List<Exercise>,
    val tip: String
)

/**
 * Builds a weekly plan sized to however many days the person picked (not
 * locked to 3). Templates rotate through a pool so 1 day and 7 days both
 * produce a sensible, non-repetitive week aimed at weight loss.
 */
object WorkoutPlanProvider {

    private val dayLabels = mapOf(
        Calendar.SATURDAY to "سبت",
        Calendar.SUNDAY to "حد",
        Calendar.MONDAY to "إتنين",
        Calendar.TUESDAY to "تلات",
        Calendar.WEDNESDAY to "أربع",
        Calendar.THURSDAY to "خميس",
        Calendar.FRIDAY to "جمعة"
    )

    // Order used to walk the week starting Saturday, matching MainActivity's chip order.
    private val weekOrder = listOf(
        Calendar.SATURDAY, Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY,
        Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY
    )

    private fun template(focus: String): (String) -> DayPlan {
        return { label ->
            when (focus) {
                "FULL_A" -> DayPlan(
                    label, "فل بادي + قوة", "40-45 دقيقة",
                    listOf(
                        Exercise("إحماء (مشي سريع/سكيبينج)", "5-7 دقايق"),
                        Exercise("سكوات (Squat)", "3×12"),
                        Exercise("ضغط (Push-up)", "3×10"),
                        Exercise("Lunges لكل رجل", "3×10"),
                        Exercise("Plank", "3×30 ثانية"),
                        Exercise("مشي على السير (Incline)", "10 دقايق")
                    ),
                    "ركز إن الحركة تبقى صح قبل ما تزود الوزن."
                )
                "CARDIO_CORE" -> DayPlan(
                    label, "كارديو + كور", "35-40 دقيقة",
                    listOf(
                        Exercise("إحماء", "5 دقايق"),
                        Exercise("كارديو متقطع (جري خفيف/مشي سريع)", "20 دقيقة (1د جري / 2د مشي)"),
                        Exercise("Mountain Climbers", "3×20"),
                        Exercise("Russian Twists", "3×20"),
                        Exercise("Bicycle Crunches", "3×15"),
                        Exercise("تهدئة وتمدد", "5 دقايق")
                    ),
                    "الاستمرارية أهم من السرعة في يوم الكارديو."
                )
                "FULL_B" -> DayPlan(
                    label, "فل بادي + قوة تانية", "40-45 دقيقة",
                    listOf(
                        Exercise("إحماء", "5-7 دقايق"),
                        Exercise("Deadlift بدمبل أو بار خفيف", "3×10"),
                        Exercise("Shoulder Press بدمبل", "3×10"),
                        Exercise("Glute Bridge", "3×15"),
                        Exercise("Side Plank لكل جنب", "3×20 ثانية"),
                        Exercise("Jumping Jacks", "3×30 ثانية"),
                        Exercise("تهدئة وتمدد", "5 دقايق")
                    ),
                    "اشرب مياه كفاية طول اليوم ونام 7 ساعات على الأقل."
                )
                "UPPER" -> DayPlan(
                    label, "الجزء العلوي", "35-40 دقيقة",
                    listOf(
                        Exercise("إحماء", "5 دقايق"),
                        Exercise("ضغط (Push-up)", "3×12"),
                        Exercise("Shoulder Press بدمبل", "3×10"),
                        Exercise("Bent-over Row بدمبل", "3×12"),
                        Exercise("Tricep Dips على كرسي", "3×10"),
                        Exercise("Plank", "3×30 ثانية")
                    ),
                    "لو مفيش دمبل، استخدم زجاجات مياه مليانة كبديل."
                )
                "LOWER" -> DayPlan(
                    label, "الجزء السفلي", "35-40 دقيقة",
                    listOf(
                        Exercise("إحماء", "5 دقايق"),
                        Exercise("سكوات (Squat)", "4×12"),
                        Exercise("Lunges لكل رجل", "3×12"),
                        Exercise("Glute Bridge", "3×15"),
                        Exercise("Calf Raises", "3×20"),
                        Exercise("تمدد للرجلين", "5 دقايق")
                    ),
                    "خد راحة أطول شوية بين المجموعات في يوم الرجلين."
                )
                "HIIT" -> DayPlan(
                    label, "كارديو HIIT", "25-30 دقيقة",
                    listOf(
                        Exercise("إحماء", "5 دقايق"),
                        Exercise("Jumping Jacks", "40 ثانية عمل / 20 راحة ×6"),
                        Exercise("High Knees", "40 ثانية عمل / 20 راحة ×6"),
                        Exercise("Burpees (مبسطة)", "30 ثانية عمل / 30 راحة ×6"),
                        Exercise("تهدئة وتمدد", "5 دقايق")
                    ),
                    "يوم شديد — لو حسيت بدوخة أو تعب زيادة، وقف واستريح."
                )
                else -> DayPlan(
                    label, "كور + مرونة (يوم خفيف)", "20-25 دقيقة",
                    listOf(
                        Exercise("مشي خفيف", "10 دقايق"),
                        Exercise("Plank", "3×30 ثانية"),
                        Exercise("Cat-Cow Stretch", "10 مرات"),
                        Exercise("تمدد كامل للجسم", "10 دقايق")
                    ),
                    "الأيام الخفيفة دي بتساعد العضلات تتعافى وتمنع الإصابات."
                )
            }
        }
    }

    /**
     * pool order chosen so common day-counts (1..7) land on a balanced mix
     * rather than just repeating the same 2-3 workouts.
     */
    private val pool = listOf(
        "FULL_A", "CARDIO_CORE", "FULL_B", "UPPER", "LOWER", "HIIT", "CORE_MOBILITY"
    )

    /** selectedDays: Calendar.SATURDAY..FRIDAY constants the user picked, any count 1-7. */
    fun generatePlan(selectedDays: Set<Int>): List<DayPlan> {
        val ordered = weekOrder.filter { it in selectedDays }
        return ordered.mapIndexed { index, dayConst ->
            val focus = pool[index % pool.size]
            template(focus)(dayLabels[dayConst] ?: "يوم")
        }
    }
}
