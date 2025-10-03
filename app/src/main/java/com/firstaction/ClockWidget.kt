package com.firstaction

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.provider.AlarmClock
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*


class ClockWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        PatternBitmapGenerator.initialize(context) // make sure cache is ready
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleNextUpdate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        context.applicationContext.registerReceiver(this, filter)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Cancel updates when last widget is removed
        cancelUpdates(context)
        // Clear bitmap cache to free memory
        PatternBitmapGenerator.clearCache()
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            intent.action == ACTION_AUTO_UPDATE) {

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, ClockWidget::class.java)
            )

            // Update all widgets
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }

            // Schedule next update
            scheduleNextUpdate(context)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.clock_widget)

        // Get current time
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Extract individual digits
        val hourTens = hour / 10
        val hourOnes = hour % 10
        val minuteTens = minute / 10
        val minuteOnes = minute % 10

        // Set pattern bitmaps for each digit
        views.setImageViewBitmap(R.id.hour_tens, PatternBitmapGenerator.getDigitBitmap(hourTens))
        views.setImageViewBitmap(R.id.hour_ones, PatternBitmapGenerator.getDigitBitmap(hourOnes))
        views.setImageViewBitmap(R.id.minute_tens, PatternBitmapGenerator.getDigitBitmap(minuteTens))
        views.setImageViewBitmap(R.id.minute_ones, PatternBitmapGenerator.getDigitBitmap(minuteOnes))

        // Update date
        val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        val dateString = dateFormat.format(calendar.time)
        views.setTextViewText(R.id.widget_date, dateString)

        // Set click intent to open clock app
        val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            clockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun scheduleNextUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ClockWidget::class.java).apply {
            action = ACTION_AUTO_UPDATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next minute (at 0 seconds)
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Cancel any existing alarms
        alarmManager.cancel(pendingIntent)

        // Schedule exact alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ - check if permission is granted
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                // Android 11 and below
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelUpdates(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClockWidget::class.java).apply {
            action = ACTION_AUTO_UPDATE
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private const val ACTION_AUTO_UPDATE = "com.firstaction.ACTION_AUTO_UPDATE"
    }
}

