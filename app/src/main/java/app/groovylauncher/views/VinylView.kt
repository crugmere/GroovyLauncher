package app.groovylauncher.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import kotlin.math.*

class VinylView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var startTime = System.currentTimeMillis()

    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            invalidate()
            choreographer.postFrameCallback(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startTime = System.currentTimeMillis()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val t = (System.currentTimeMillis() - startTime) / 1000f
        val cx = width / 2f
        val cy = height / 2f
        val radius = minOf(cx, cy) * 0.85f

        canvas.save()
        canvas.rotate(t * 33f / 60f * 360f, cx, cy)
        drawRecord(canvas, cx, cy, radius, t)
        canvas.restore()

        drawLabel(canvas, cx, cy, radius * 0.22f, t)
    }

    private fun drawRecord(canvas: Canvas, cx: Float, cy: Float, radius: Float, t: Float) {
        // Main vinyl body
        paint.shader = null
        paint.color = Color.argb(230, 15, 5, 25)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, radius, paint)

        // Grooves with subtle rainbow hue shift
        paint.style = Paint.Style.STROKE
        val grooveCount = 28
        for (i in 0..grooveCount) {
            val fraction = i / grooveCount.toFloat()
            val grooveRadius = radius * (0.28f + fraction * 0.68f)
            val hue = (fraction * 120f + t * 15f) % 360f
            paint.color = Color.HSVToColor(30, floatArrayOf(hue, 0.6f, 0.7f))
            paint.strokeWidth = 1.2f
            canvas.drawCircle(cx, cy, grooveRadius, paint)
        }

        // Ornamental spoke ring (Fillmore poster style)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        val spokeRingRadius = radius * 0.75f
        val spokeCount = 24
        for (i in 0 until spokeCount) {
            val angle = (i * 360f / spokeCount) * PI.toFloat() / 180f
            val innerR = radius * 0.28f
            val outerR = spokeRingRadius
            paint.color = Color.argb(60, 255, 215, 0)
            canvas.drawLine(
                cx + cos(angle) * innerR,
                cy + sin(angle) * innerR,
                cx + cos(angle) * outerR,
                cy + sin(angle) * outerR,
                paint
            )
        }

        // Spoke ring circle
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        paint.color = Color.argb(120, 255, 215, 0)
        canvas.drawCircle(cx, cy, spokeRingRadius, paint)
        canvas.drawCircle(cx, cy, radius * 0.28f, paint)

        // Outer edge glow
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.argb(80, 180, 0, 255)
        canvas.drawCircle(cx, cy, radius, paint)
    }

    private fun drawLabel(canvas: Canvas, cx: Float, cy: Float, radius: Float, t: Float) {
        // Cycling label colour
        val hue = (t * 25f) % 360f
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Color.HSVToColor(255, floatArrayOf(hue, 0.8f, 0.9f))
        canvas.drawCircle(cx, cy, radius, paint)

        // Label shine
        val shineGradient = RadialGradient(
            cx - radius * 0.3f, cy - radius * 0.3f, radius * 0.8f,
            intArrayOf(Color.argb(80, 255, 255, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = shineGradient
        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null

        // Centre hole
        paint.color = Color.argb(220, 10, 0, 20)
        canvas.drawCircle(cx, cy, radius * 0.18f, paint)
    }
}