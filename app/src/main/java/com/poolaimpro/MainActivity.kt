package com.poolaimpro

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var aim: AimCanvasView
    private lateinit var lengthLabel: TextView
    private lateinit var angleLabel: TextView
    private var syncing = false
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); buildUi() }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.rgb(13, 24, 31))
        }
        root.addView(TextView(this).apply {
            text = "POOL AIM PRO"; textSize = 25f; gravity = Gravity.CENTER
            setTextColor(Color.rgb(57, 235, 119))
        }, LinearLayout.LayoutParams(-1, dp(48)))

        root.addView(TextView(this).apply {
            text = "Manual control • Drag white start or yellow goal"
            textSize = 13f; gravity = Gravity.CENTER; setTextColor(Color.LTGRAY)
        }, LinearLayout.LayoutParams(-1, dp(28)))

        aim = AimCanvasView(this)
        root.addView(aim, LinearLayout.LayoutParams(-1, 0, 1f))

        lengthLabel = label("Length")
        root.addView(lengthLabel)
        val length = SeekBar(this).apply { max = 1480; progress = 230 }
        root.addView(length)

        angleLabel = label("Angle")
        root.addView(angleLabel)
        val angle = SeekBar(this).apply { max = 359 }
        root.addView(angle)

        aim.onAimChanged = { l, a ->
            lengthLabel.text = "Length: ${l.toInt()} px"
            angleLabel.text = "Angle: ${a.toInt()}°"
            if (!syncing) {
                syncing = true
                length.progress = (l.toInt() - 20).coerceIn(0, 1480)
                angle.progress = a.toInt().coerceIn(0, 359)
                syncing = false
            }
        }
        length.setOnSeekBarChangeListener(listener { if (!syncing) aim.setAimLength((it + 20).toFloat()) })
        angle.setOnSeekBarChangeListener(listener { if (!syncing) aim.setAimAngle(it.toFloat()) })

        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        buttons.addView(Button(this).apply {
            text = "Reset"; setOnClickListener { aim.resetAim() }
        }, LinearLayout.LayoutParams(0, dp(52), 1f))
        buttons.addView(Button(this).apply {
            text = "Start Floating Overlay"; setOnClickListener { startOverlay() }
        }, LinearLayout.LayoutParams(0, dp(52), 1.6f))
        root.addView(buttons)

        root.addView(TextView(this).apply {
            text = "Manual guide only. No automatic detection or guaranteed wins."
            textSize = 12f; gravity = Gravity.CENTER; setTextColor(Color.LTGRAY)
        }, LinearLayout.LayoutParams(-1, dp(42)))
        setContentView(root)
    }

    private fun label(s: String) = TextView(this).apply {
        text = s; textSize = 14f; setTextColor(Color.WHITE)
    }

    private fun listener(change: (Int) -> Unit) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { change(progress) }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            Toast.makeText(this, "Allow display over other apps, then tap Start Floating Overlay again.", Toast.LENGTH_LONG).show()
            return
        }
        startService(Intent(this, OverlayService::class.java)
            .putExtra("length", aim.length()).putExtra("angle", aim.angle()))
    }
}
