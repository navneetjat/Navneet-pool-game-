package com.poolaimpro

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var rootView: LinearLayout? = null

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return }
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlay()
    }

    private fun createOverlay() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(8), dp(6), dp(8), dp(8))
            setBackgroundColor(0xE916222B.toInt())
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply {
            text = "POOL AIM PRO  ⋮⋮"; textSize = 14f; setTextColor(Color.GREEN)
            setPadding(dp(5), 0, dp(5), 0)
        }, LinearLayout.LayoutParams(0, dp(44), 1f))
        header.addView(Button(this).apply {
            text = "X"; setOnClickListener { stopSelf() }
        }, LinearLayout.LayoutParams(dp(48), dp(44)))
        root.addView(header)

        val aim = AimCanvasView(this)
        aim.setAim(intent?.getFloatExtra("length", 250f) ?: 250f, intent?.getFloatExtra("angle", 0f) ?: 0f)
        root.addView(aim, LinearLayout.LayoutParams(dp(280), dp(200)))

        val lengthLabel = label("Length")
        root.addView(lengthLabel)
        root.addView(SeekBar(this).apply {
            max = 1000; progress = ((aim.length().toInt() - 20) / 1).coerceIn(0, 1000)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    val value = (p + 20).toFloat(); aim.setAimLength(value); lengthLabel.text = "Length: ${value.toInt()}"
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })

        val angleLabel = label("Angle")
        root.addView(angleLabel)
        root.addView(SeekBar(this).apply {
            max = 359; progress = aim.angle().toInt().coerceIn(0, 359)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                    aim.setAimAngle(p.toFloat()); angleLabel.text = "Angle: ${p}°"
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        })

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(Button(this).apply {
            text = "Reset"; setOnClickListener { aim.setAim(250f, 0f) }
        }, LinearLayout.LayoutParams(0, dp(44), 1f))
        actions.addView(Button(this).apply {
            text = "Hide"; setOnClickListener { root.visibility = View.GONE }
        }, LinearLayout.LayoutParams(0, dp(44), 1f))
        root.addView(actions)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = dp(12); y = dp(80) }

        header.setOnTouchListener(object : View.OnTouchListener {
            private var ox = 0; private var oy = 0; private var tx = 0f; private var ty = 0f
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> { ox = params.x; oy = params.y; tx = e.rawX; ty = e.rawY; return true }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = ox + (e.rawX - tx).toInt(); params.y = oy + (e.rawY - ty).toInt()
                        try { wm.updateViewLayout(root, params) } catch (_: Exception) {}
                        return true
                    }
                    MotionEvent.ACTION_UP -> return true
                }
                return false
            }
        })

        rootView = root
        try { wm.addView(root, params) } catch (e: Exception) {
            Toast.makeText(this, "Overlay failed: ${e.message ?: "unknown"}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text; textSize = 12f; setTextColor(Color.WHITE); setPadding(dp(3), dp(3), dp(3), 0)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY
    override fun onDestroy() {
        rootView?.let { try { if (it.parent != null) wm.removeView(it) } catch (_: Exception) {} }
        rootView = null
        super.onDestroy()
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
