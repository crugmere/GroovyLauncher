package app.groovylauncher.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View

class GroovyBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var startTime = System.currentTimeMillis()
    private var screenBitmap: Bitmap? = null
    private var bitmapCanvas: Canvas? = null

    // Lava blob data
    private data class Blob(
        val speedX: Float, val speedY: Float,
        val baseSize: Float, val hue: Float,
        val phaseX: Float, val phaseY: Float,
        val sizePhase: Float
    )

    private val blobs = listOf(
        Blob(0.3f, 0.2f, 0.18f, 280f, 0f, 1f, 0f),
        Blob(0.2f, 0.35f, 0.15f, 320f, 2f, 0.5f, 1f),
        Blob(0.25f, 0.15f, 0.20f, 200f, 4f, 2f, 2f),
        Blob(0.15f, 0.28f, 0.16f, 350f, 1f, 3f, 3f),
        Blob(0.35f, 0.22f, 0.17f, 260f, 3f, 1.5f, 4f)
    )

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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenBitmap?.recycle()
        screenBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmapCanvas = Canvas(screenBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val t = (System.currentTimeMillis() - startTime) / 1000f
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        // Draw deep indigo background
        canvas.drawColor(Color.rgb(13, 0, 24))

        // Draw plasma layer
        drawPlasma(canvas, t, w, h)

        // Draw lava blobs
        drawBlobs(canvas, t, w, h)
    }

    private fun drawPlasma(canvas: Canvas, t: Float, w: Float, h: Float) {
        val st = t / 8f
        val cols = 24
        val rows = 42
        val cellW = w / cols
        val cellH = h / rows

        for (row in 0..rows) {
            for (col in 0..cols) {
                val x = col / cols.toFloat()
                val y = row / rows.toFloat()

                val v1 = Math.sin((x * 6 + st).toDouble()).toFloat()
                val v2 = Math.sin((y * 4 + st * 0.7).toDouble()).toFloat()
                val v3 = Math.sin(((x + y) * 5 + st * 1.3).toDouble()).toFloat()
                val v4 = Math.sin((Math.sqrt((x - 0.5 + Math.sin(st * 0.5) * 0.3) * (x - 0.5 + Math.sin(st * 0.5) * 0.3) + (y - 0.5 + Math.cos(st * 0.4) * 0.3) * (y - 0.5 + Math.cos(st * 0.4) * 0.3)) * 8).toDouble()).toFloat()

                val value = (v1 + v2 + v3 + v4) / 4f
                val hue = (value * 60f + 280f + st * 20f) % 360f
                val sat = 0.8f + value * 0.2f
                val bright = 0.3f + value * 0.25f

                paint.color = Color.HSVToColor(floatArrayOf(
                    ((hue % 360f) + 360f) % 360f,
                    sat.coerceIn(0f, 1f),
                    bright.coerceIn(0f, 1f)
                ))
                paint.alpha = 180

                canvas.drawRect(
                    col * cellW, row * cellH,
                    (col + 1) * cellW + 1, (row + 1) * cellH + 1,
                    paint
                )
            }
        }
    }

    private fun drawBlobs(canvas: Canvas, t: Float, w: Float, h: Float) {
        val st = t / 8f

        for (blob in blobs) {
            val x = ((Math.sin((st * blob.speedX + blob.phaseX).toDouble()) * 0.4 + 0.5) * w).toFloat()
            val y = ((Math.cos((st * blob.speedY + blob.phaseY).toDouble()) * 0.4 + 0.5) * h).toFloat()
            val sizePulse = (Math.sin((st * 2 + blob.sizePhase).toDouble()) * 0.15 + 1.0).toFloat()
            val radius = blob.baseSize * w * sizePulse

            val hue = (blob.hue + st * 30f) % 360f

            val gradient = RadialGradient(
                x, y, radius,
                intArrayOf(
                    Color.HSVToColor(200, floatArrayOf(hue, 1f, 1f)),
                    Color.HSVToColor(80, floatArrayOf(hue, 0.8f, 0.8f)),
                    Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )

            paint.shader = gradient
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
            canvas.drawCircle(x, y, radius, paint)
        }

        paint.shader = null
        paint.xfermode = null
    }
}