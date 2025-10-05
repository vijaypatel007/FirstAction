// CLOCK WIDGET (only showing the key methods and companion)
package com.firstaction

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class ClockWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        PatternBitmapGenerator.initialize(context) // ensure generator ready

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleNextUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_AUTO_UPDATE || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, ClockWidget::class.java))
            for (id in ids) updateAppWidget(context, mgr, id)
            scheduleNextUpdate(context)
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val hourTens = hour / 10
        val hourOnes = hour % 10
        val minuteTens = minute / 10
        val minuteOnes = minute % 10

        // get last digits
        val last = WidgetPrefs.getLastDigits(context)
        val lastHourTens = last[0]
        val lastHourOnes = last[1]
        val lastMinTens = last[2]
        val lastMinOnes = last[3]

        // If any changed, animate; otherwise just set bitmaps
        val changed = (lastHourTens != hourTens) || (lastHourOnes != hourOnes) ||
                (lastMinTens != minuteTens) || (lastMinOnes != minuteOnes)

        if (changed && lastHourTens != -1) {
            // animate each digit separately and push frames quickly
            animateDigits(context, appWidgetManager, appWidgetId,
                lastHourTens, hourTens,
                lastHourOnes, hourOnes,
                lastMinTens, minuteTens,
                lastMinOnes, minuteOnes)
        } else {
            // static update
            val views = RemoteViews(context.packageName, R.layout.clock_widget)
            views.setImageViewBitmap(R.id.hour_tens, PatternBitmapGenerator.getDigitBitmap(hourTens))
            views.setImageViewBitmap(R.id.hour_ones, PatternBitmapGenerator.getDigitBitmap(hourOnes))
            views.setImageViewBitmap(R.id.minute_tens, PatternBitmapGenerator.getDigitBitmap(minuteTens))
            views.setImageViewBitmap(R.id.minute_ones, PatternBitmapGenerator.getDigitBitmap(minuteOnes))

            val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            views.setTextViewText(R.id.widget_date, dateFormat.format(calendar.time))

            // click opens alarms
            val clockIntent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, clockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        // save last digits
        WidgetPrefs.saveLastDigits(context, hourTens, hourOnes, minuteTens, minuteOnes)
    }

    private fun animateDigits(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        fromHT: Int, toHT: Int,
        fromHO: Int, toHO: Int,
        fromMT: Int, toMT: Int,
        fromMO: Int, toMO: Int
    ) {
        // Generate frames in background thread to avoid blocking broadcast receiver
        Thread {
            try {
                // For each digit generate frames
                val framesHT = if (fromHT in 0..9) PatternBitmapGenerator.generateAnimatedDigitFrames(context, fromHT, toHT) else null
                val framesHO = if (fromHO in 0..9) PatternBitmapGenerator.generateAnimatedDigitFrames(context, fromHO, toHO) else null
                val framesMT = if (fromMT in 0..9) PatternBitmapGenerator.generateAnimatedDigitFrames(context, fromMT, toMT) else null
                val framesMO = if (fromMO in 0..9) PatternBitmapGenerator.generateAnimatedDigitFrames(context, fromMO, toMO) else null

                val framesCount = framesHT?.size ?: framesHO?.size ?: framesMT?.size ?: framesMO?.size ?: 1
                for (f in 0 until framesCount) {
                    val v = RemoteViews(context.packageName, R.layout.clock_widget)
                    // if frames available, use frame[f] else fallback to static target
                    if (framesHT != null) v.setImageViewBitmap(R.id.hour_tens, framesHT[f]) else v.setImageViewBitmap(R.id.hour_tens, PatternBitmapGenerator.getDigitBitmap(toHT))
                    if (framesHO != null) v.setImageViewBitmap(R.id.hour_ones, framesHO[f]) else v.setImageViewBitmap(R.id.hour_ones, PatternBitmapGenerator.getDigitBitmap(toHO))
                    if (framesMT != null) v.setImageViewBitmap(R.id.minute_tens, framesMT[f]) else v.setImageViewBitmap(R.id.minute_tens, PatternBitmapGenerator.getDigitBitmap(toMT))
                    if (framesMO != null) v.setImageViewBitmap(R.id.minute_ones, framesMO[f]) else v.setImageViewBitmap(R.id.minute_ones, PatternBitmapGenerator.getDigitBitmap(toMO))

                    // date text uses current time
                    val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
                    v.setTextViewText(R.id.widget_date, dateFormat.format(Date()))

                    // click handler
                    val clockIntent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
                    val pendingIntent = PendingIntent.getActivity(context, 0, clockIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    v.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, v)

                    // Sleep between frames (small)
                    Thread.sleep(PatternBitmapGenerator.FRAME_MS)
                }
            } catch (ex: Exception) {
                Log.e("ClockWidget", "Animation error: ${ex.message}", ex)
            } finally {
                // Ensure final state is set to target bitmaps
                val finalViews = RemoteViews(context.packageName, R.layout.clock_widget)
                finalViews.setImageViewBitmap(R.id.hour_tens, PatternBitmapGenerator.getDigitBitmap(toHT))
                finalViews.setImageViewBitmap(R.id.hour_ones, PatternBitmapGenerator.getDigitBitmap(toHO))
                finalViews.setImageViewBitmap(R.id.minute_tens, PatternBitmapGenerator.getDigitBitmap(toMT))
                finalViews.setImageViewBitmap(R.id.minute_ones, PatternBitmapGenerator.getDigitBitmap(toMO))
                val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
                finalViews.setTextViewText(R.id.widget_date, dateFormat.format(Date()))
                val clockIntent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
                val pendingIntent = PendingIntent.getActivity(context, 0, clockIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                finalViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                appWidgetManager.updateAppWidget(appWidgetId, finalViews)
            }
        }.start()
    }

    private fun scheduleNextUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ClockWidget::class.java).apply { action = ACTION_AUTO_UPDATE }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // next minute (0 seconds)
        val next = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        alarmManager.cancel(pendingIntent)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val ACTION_AUTO_UPDATE = "com.firstaction.ACTION_AUTO_UPDATE"
    }
}
