package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Service to show a draggable floating AI orb over all applications
 * using Android's SYSTEM_ALERT_WINDOW (Overlay) permission.
 *
 * Tapping the orb opens MYRA AI and brings it to foreground.
 * Dragging allows moving the orb freely around the screen edges.
 */
class MyraFloatingOrbService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingOrbView: View? = null
    private var params: WindowManager.LayoutParams? = null

    companion object {
        const val ACTION_SHOW = "com.example.service.action.SHOW_ORB"
        const val ACTION_HIDE = "com.example.service.action.HIDE_ORB"

        private val _isOrbVisible = MutableStateFlow(false)
        val isOrbVisible: StateFlow<Boolean> = _isOrbVisible.asStateFlow()

        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        fun requestOverlayPermission(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_HIDE -> {
                removeFloatingOrb()
                stopSelf()
            }
            else -> {
                showFloatingOrb()
            }
        }
        return START_NOT_STICKY
    }

    private fun showFloatingOrb() {
        if (!canDrawOverlays(this)) {
            Log.w("MyraFloatingOrb", "Overlay permission not granted")
            stopSelf()
            return
        }

        if (floatingOrbView != null) return // Already showing

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val sizePx = (64 * resources.displayMetrics.density).toInt()

        params = WindowManager.LayoutParams(
            sizePx,
            sizePx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (resources.displayMetrics.widthPixels - sizePx - 20)
            y = (resources.displayMetrics.heightPixels / 3)
        }

        val orbContainer = FrameLayout(this).apply {
            val bgDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                colors = intArrayOf(
                    0xFFBD00FF.toInt(), // Neon Purple
                    0xFFE040FB.toInt(), // Magenta
                    0xFF00F0FF.toInt()  // Cyan accent
                )
                gradientType = GradientDrawable.SWEEP_GRADIENT
                setStroke((2 * resources.displayMetrics.density).toInt(), 0xFFFFFFFF.toInt())
            }
            background = bgDrawable
            elevation = 16f
        }

        val icon = ImageView(this).apply {
            setImageResource(R.mipmap.ic_launcher)
            contentDescription = "MYRA AI Floating Orb"
            val pad = (12 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }
        orbContainer.addView(icon)

        // Setup touch & drag listeners
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        orbContainer.setOnTouchListener { _, event ->
            val layoutParams = params ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (abs(dx) > 10 || abs(dy) > 10) {
                        isClick = false
                    }
                    layoutParams.x = initialX + dx
                    layoutParams.y = initialY + dy
                    windowManager?.updateViewLayout(floatingOrbView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        openMyraApp()
                    }
                    true
                }
                else -> false
            }
        }

        try {
            windowManager?.addView(orbContainer, params)
            floatingOrbView = orbContainer
            _isOrbVisible.value = true
        } catch (e: Exception) {
            Log.e("MyraFloatingOrb", "Failed to add overlay window", e)
        }
    }

    private fun openMyraApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun removeFloatingOrb() {
        try {
            if (floatingOrbView != null) {
                windowManager?.removeView(floatingOrbView)
                floatingOrbView = null
                _isOrbVisible.value = false
            }
        } catch (e: Exception) {
            Log.e("MyraFloatingOrb", "Error removing overlay", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeFloatingOrb()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
