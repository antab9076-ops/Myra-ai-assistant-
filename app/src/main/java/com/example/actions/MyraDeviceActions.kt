package com.example.actions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

sealed class DeviceActionResult {
    data class Executed(val message: String) : DeviceActionResult()
    data class NeedsConfirmation(val actionPrompt: String, val pendingIntent: Intent, val confirmationType: String) : DeviceActionResult()
    data class Error(val error: String) : DeviceActionResult()
}

object MyraDeviceActions {

    fun parseAndExecuteCommand(context: Context, rawCommand: String): DeviceActionResult? {
        val lower = rawCommand.lowercase().trim()

        // 1. Stop Listening / Pause Assistant
        if (lower == "stop listening" || lower == "stop" || lower == "go to sleep" || lower == "mute" || lower == "quiet") {
            return DeviceActionResult.Executed("Stopping listening now. You can tap the mic or say 'Hey MYRA' when you need me.")
        }

        // 2. Tell me the time / date
        if (lower.contains("tell me the time") || lower.contains("what time is it") || lower.contains("what is the time") || lower == "time") {
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            val currentTime = sdf.format(java.util.Date())
            return DeviceActionResult.Executed("It is currently $currentTime.")
        }

        if (lower.contains("what is today's date") || lower.contains("what day is it") || lower.contains("what is the date") || lower == "date") {
            val sdf = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault())
            val currentDate = sdf.format(java.util.Date())
            return DeviceActionResult.Executed("Today is $currentDate.")
        }

        // 3. Open / Launch Applications (with support for YouTube, Settings, Camera, etc.)
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ")) {
            val appQuery = lower.removePrefix("open ").removePrefix("launch ").removePrefix("start ").trim()
            return handleOpenApp(context, appQuery)
        }

        // 4. Set Alarm / Reminder
        if (lower.contains("set alarm") || lower.contains("set an alarm") || lower.contains("wake me up")) {
            return handleSetAlarm(context, lower)
        }

        // 5. Set Timer
        if (lower.contains("set timer") || lower.contains("set a timer") || lower.contains("start timer")) {
            return handleSetTimer(context, lower)
        }

        // 6. Open Camera
        if (lower.contains("open camera") || lower.contains("take a photo") || lower.contains("take photo")) {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                DeviceActionResult.Executed("Opening camera...")
            } catch (e: Exception) {
                DeviceActionResult.Error("Could not open camera: ${e.message}")
            }
        }

        // 7. Battery Optimization Settings
        if (lower.contains("battery optimization") || lower.contains("battery settings")) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                DeviceActionResult.Executed("Opening battery optimization settings so MYRA can run in the background...")
            } catch (e: Exception) {
                DeviceActionResult.Error("Could not open battery settings: ${e.message}")
            }
        }

        // 8. Wi-Fi / Bluetooth / General Settings
        if (lower.contains("open settings") || lower.contains("open system settings")) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                DeviceActionResult.Executed("Opening system settings...")
            } catch (e: Exception) {
                DeviceActionResult.Error("Could not open settings: ${e.message}")
            }
        }

        // Not a device command; pass through to Gemini reasoning
        return null
    }

    private fun handleOpenApp(context: Context, appQuery: String): DeviceActionResult {
        val pm: PackageManager = context.packageManager

        // Known standard package maps
        val knownPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "maps" to "com.google.android.apps.maps",
            "google maps" to "com.google.android.apps.maps",
            "chrome" to "com.android.chrome",
            "browser" to "com.android.chrome",
            "gmail" to "com.google.android.gm",
            "email" to "com.google.android.gm",
            "play store" to "com.android.vending",
            "google play" to "com.android.vending",
            "camera" to "com.google.android.GoogleCamera",
            "calendar" to "com.google.android.calendar",
            "calculator" to "com.google.android.calculator",
            "clock" to "com.google.android.deskclock",
            "spotify" to "com.spotify.music"
        )

        val targetPackage = knownPackages[appQuery]
        if (targetPackage != null) {
            val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                return try {
                    context.startActivity(launchIntent)
                    DeviceActionResult.Executed("Opening ${appQuery.replaceFirstChar { it.uppercase() }}...")
                } catch (e: Exception) {
                    DeviceActionResult.Error("Unable to open $appQuery: ${e.message}")
                }
            }
        }

        // Search installed applications for matching label
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        for (appInfo in installedApps) {
            val label = pm.getApplicationLabel(appInfo).toString().lowercase()
            if (label == appQuery || label.contains(appQuery)) {
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    return try {
                        context.startActivity(launchIntent)
                        DeviceActionResult.Executed("Opening ${pm.getApplicationLabel(appInfo)}...")
                    } catch (e: Exception) {
                        DeviceActionResult.Error("Unable to launch $label: ${e.message}")
                    }
                }
            }
        }

        return DeviceActionResult.Error("I could not find an app matching \"$appQuery\" on your device.")
    }

    private fun handleSetAlarm(context: Context, text: String): DeviceActionResult {
        // Simple regex to parse hour and minute like "7:30", "8 am", "6 pm", "7 o'clock"
        val timeRegex = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?")
        val match = timeRegex.find(text)

        if (match != null) {
            var hour = match.groupValues[1].toIntOrNull() ?: 8
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val ampm = match.groupValues[3].lowercase()

            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0

            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, "MYRA AI Alarm")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            return try {
                context.startActivity(intent)
                val formattedTime = String.format("%02d:%02d", hour, minute)
                DeviceActionResult.Executed("Setting alarm for $formattedTime.")
            } catch (e: Exception) {
                DeviceActionResult.Error("Could not schedule alarm: ${e.message}")
            }
        } else {
            return DeviceActionResult.Error("Please specify the alarm time, for example: 'Set alarm for 7:30 AM'.")
        }
    }

    private fun handleSetTimer(context: Context, text: String): DeviceActionResult {
        val minRegex = Regex("(\\d+)\\s*(?:minute|min)")
        val secRegex = Regex("(\\d+)\\s*(?:second|sec)")

        val minutes = minRegex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val seconds = secRegex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val totalSeconds = (minutes * 60) + seconds

        if (totalSeconds > 0) {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, totalSeconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, "MYRA AI Timer")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            return try {
                context.startActivity(intent)
                DeviceActionResult.Executed("Starting a timer for $totalSeconds seconds.")
            } catch (e: Exception) {
                DeviceActionResult.Error("Could not start timer: ${e.message}")
            }
        }

        return DeviceActionResult.Error("Please specify timer duration, for example: 'Set timer for 5 minutes'.")
    }
}
