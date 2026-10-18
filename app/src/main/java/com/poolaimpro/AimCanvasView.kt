package com.poolaimpro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class AimCanvasView(context: Context) : View(context) {
    private val tablePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(0, 145, 164) }
    private val railPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(130, 30, 38); style = Paint.Style.STROKE; strokeWidth = dp(12f)
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; strokeWidth = dp(2.5f); style = Paint.Style.STROKE
    }
    private val startPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val goalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.YELLOW }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = dp(2f)
    }

    private var sx = 0f
    private var sy = 0f
    private var gx = 0f
    private var gy = 0f
    private var initialized = false
    private var selected = 0
    private var aimLength = 250f
    private var aimAngle = 0f
    var onAimChanged: ((Float, Float) -> Unit)? = null

    private fun dp(v: Float) = v * resources.displayMetrics.density
    fun length() = aimLength
    fun angle() = aimAngle

    fun setAimLength(v: Float) { aimLength = v.coerceIn(20f, 1500f); updateGoal(); invalidate(); report() }
    fun setAimAngle(v: Float) { aimAngle = ((v % 360f) + 360f) % 360f; updateGoal(); invalidate(); report() }
    fun setAim(length: Float, angle: Float) {
        aimLength = length.coerceIn(20f, 1500f)
        aimAngle = ((angle % 360f) + 360f) % 360f
        updateGoal(); invalidate(); report()
    }

    fun resetAim() {
        if (width > 0 && height > 0) {
            sx = width * .25f; sy = height * .55f
            aimLength = width * .45f; aimAngle = 0f
            initialized = true; updateGoal(); invalidate(); report()
        }
    }

    private fun initialize() {
        if (!initialized && width > 0 && height > 0) {
            sx = width * .25f; sy = height * .55f
            aimLength = width * .45f; aimAngle = 0f
            initialized = true; updateGoal()
        }
    }

    private fun updateGoal() {
        gx = sx + cos(Math.toRadians(aimAngle.toDouble())).toFloat() * aimLength
        gy = sy + sin(Math.toRadians(aimAngle.toDouble())).toFloat() * aimLength
        if (width > 0 && height > 0) {
            gx = gx.coerceIn(0f, width.toFloat()); gy = gy.coerceIn(0f, height.toFloat())
        }
    }

    private fun report() { onAimChanged?.invoke(aimLength, aimAngle) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas); initialize()
        val pad = dp(12f)
        val table = RectF(pad, pad, width - pad, height - pad)
        canvas.drawRoundRect(table, dp(10f), dp(10f), tablePaint)
        canvas.drawRoundRect(table, dp(10f), dp(10f), railPaint)
        val pocket = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
        listOf(pad to pad, width - pad to pad, pad to height - pad, width - pad to height - pad)
            .forEach { (x, y) -> canvas.drawCircle(x, y, dp(8f), pocket) }
        canvas.drawLine(sx, sy, gx, gy, linePaint)
        canvas.drawCircle(sx, sy, dp(11f), startPaint); canvas.drawCircle(sx, sy, dp(11f), outlinePaint)
        canvas.drawCircle(gx, gy, dp(10f), goalPaint); canvas.drawCircle(gx, gy, dp(10f), outlinePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        initialize()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                selected = when {
                    hypot(event.x - sx, event.y - sy) < dp(45f) -> 1
                    hypot(event.x - gx, event.y - gy) < dp(45f) -> 2
                    else -> 0
                }
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (selected == 1) {
                    sx = event.x.coerceIn(0f, width.toFloat())
                    sy = event.y.coerceIn(0f, height.toFloat())
                    updateGoal()
                } else if (selected == 2) {
                    gx = event.x.coerceIn(0f, width.toFloat())
                    gy = event.y.coerceIn(0f, height.toFloat())
                    val dx = gx - sx; val dy = gy - sy
                    aimLength = hypot(dx, dy).coerceAtLeast(1f)
                    aimAngle = ((Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() % 360f) + 360f) % 360f
                }
                invalidate(); report(); return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { selected = 0; performClick(); return true }
        }
        return true
    }

    override fun performClick(): Boolean { super.performClick(); return true }
}
