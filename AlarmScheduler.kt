package com.gymlock.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {

    /** Reschedules an alarm for every day the user picked, based on saved Prefs. */
    fun rescheduleAll(context: Context) {
        val days = Prefs.getDays(context)
        val hour = Prefs.getHour(context)
        val minute = Prefs.getMinute(context)
        days.forEach { day -> scheduleForDay(context, day, hour, minute) }
    }

    /**
     * Schedules (or re-arms) the alarm for one weekday (Calendar.SUNDAY..SATURDAY)
     * at the given hour/minute. If that time already passed today, it rolls to next week.
     */
    fun scheduleForDay(context: Context, dayOfWeek: Int, hour: Int, minute: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            while (get(Calendar.DAY_OF_WEEK) != dayOfWeek || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("dayOfWeek", dayOfWeek)
        }
        val pi = PendingIntent.getBroadcast(
            context, dayOfWeek, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pi)
        } else {
            // Falls back to an inexact alarm if the user hasn't granted "Alarms & reminders"
            am.set(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pi)
        }
    }

    /** Schedules the fixed 1:00 PM daily reminder that asks the user to confirm this week's plan. */
    fun scheduleDailyPrompt(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 13)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, DailyPromptReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, 9001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pi)
        } else {
            am.set(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pi)
        }
    }

    fun cancelDay(context: Context, dayOfWeek: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, dayOfWeek, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }
}
