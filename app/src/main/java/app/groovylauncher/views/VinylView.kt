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
        val radius = minOf(cx, cy) * 0.92f

        canvas.save()
        canvas.rotate(t * 33f / 60f * 360f, cx, cy)

        // Warp — squish and skew on a slow sine wave
        val warpScale = 1f + sin(t * 1.8f) * 0.06f
        val warpSkew = sin(t * 1.3f) * 0.04f
        val matrix = Matrix()
        matrix.setScale(warpScale, 1f / warpScale, cx, cy)
        matrix.postSkew(warpSkew, warpSkew * 0.5f, cx, cy)
        canvas.concat(matrix)

        drawRecord(canvas, cx, cy, radius, t)
        canvas.restore()

        drawLabel(canvas, cx, cy, radius * 0.22f, t)
    }

    private fun drawRecord(canvas: Canvas, cx: Float, cy: Float, radius: Float, t: Float) {
        // Colour cycling body
        val bodyHue = (t * 15f) % 360f
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Color.HSVToColor(230, floatArrayOf(bodyHue, 0.6f, 0.15f))
        canvas.drawCircle(cx, cy, radius, paint)

        // Vinyl sheen
        val sheenGradient = RadialGradient(
            cx - radius * 0.2f, cy - radius * 0.2f, radius * 1.4f,
            intArrayOf(
                Color.argb(40, 80, 0, 120),
                Color.argb(15, 40, 0, 80),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = sheenGradient
        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null

        // Grooves
        paint.style = Paint.Style.STROKE
        val grooveCount = 40
        for (i in 0..grooveCount) {
            val fraction = i / grooveCount.toFloat()
            val grooveRadius = radius * (0.30f + fraction * 0.62f)
            if (i % 2 == 0) {
                paint.color = Color.argb(25, 180, 100, 255)
                paint.strokeWidth = 1.5f
            } else {
                paint.color = Color.argb(12, 0, 0, 0)
                paint.strokeWidth = 0.8f
            }
            canvas.drawCircle(cx, cy, grooveRadius, paint)
        }

        // Spoke ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        val spokeRingRadius = radius * 0.72f
        val spokeCount = 36
        for (i in 0 until spokeCount) {
            val angle = (i * 360f / spokeCount) * PI.toFloat() / 180f
            val innerR = radius * 0.30f
            paint.color = Color.argb(45, 255, 200, 0)
            canvas.drawLine(
                cx + cos(angle) * innerR, cy + sin(angle) * innerR,
                cx + cos(angle) * spokeRingRadius, cy + sin(angle) * spokeRingRadius,
                paint
            )
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.argb(100, 255, 200, 0)
        canvas.drawCircle(cx, cy, spokeRingRadius, paint)
        canvas.drawCircle(cx, cy, radius * 0.30f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.argb(60, 150, 0, 220)
        canvas.drawCircle(cx, cy, radius, paint)
    }

    private fun drawLabel(canvas: Canvas, cx: Float, cy: Float, radius: Float, t: Float) {
        val hue = (t * 25f) % 360f
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Color.HSVToColor(255, floatArrayOf(hue, 0.85f, 0.75f))
        canvas.drawCircle(cx, cy, radius, paint)

        val shineGradient = RadialGradient(
            cx - radius * 0.25f, cy - radius * 0.35f, radius,
            intArrayOf(
                Color.argb(120, 255, 255, 255),
                Color.argb(20, 255, 255, 255),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.4f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = shineGradient
        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.argb(80, 255, 255, 255)
        canvas.drawCircle(cx, cy, radius * 0.85f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(8, 3, 14)
        canvas.drawCircle(cx, cy, radius * 0.2f, paint)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.argb(60, 255, 255, 255)
        canvas.drawCircle(cx, cy, radius * 0.2f, paint)
    }
}