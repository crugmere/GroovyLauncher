package app.groovylauncher.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import kotlin.math.*

class MushroomView @JvmOverloads constructor(
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

    // 5 mushrooms with independent bob phases and hues
    private data class Mushroom(
        val xFraction: Float,
        val bobPhase: Float,
        val hue: Float,
        val scale: Float
    )

    private val mushrooms = listOf(
        Mushroom(0.10f, 0.0f, 280f, 1.0f),
        Mushroom(0.28f, 1.2f, 320f, 0.85f),
        Mushroom(0.50f, 2.4f, 260f, 1.1f),
        Mushroom(0.72f, 0.8f, 300f, 0.9f),
        Mushroom(0.90f, 1.8f, 340f, 0.95f)
    )

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
        val w = width.toFloat()
        val h = height.toFloat()

        for (mushroom in mushrooms) {
            val x = mushroom.xFraction * w
            val bobOffset = sin(t * 2.1f + mushroom.bobPhase) * h * 0.06f
            val baseY = h * 0.85f + bobOffset
            val hue = (mushroom.hue + t * 20f) % 360f
            drawMushroom(canvas, x, baseY, h * 0.35f * mushroom.scale, hue, t)
        }
    }

    private fun drawMushroom(canvas: Canvas, x: Float, baseY: Float, size: Float, hue: Float, t: Float) {
        val stemWidth = size * 0.22f
        val stemHeight = size * 0.55f
        val capRadius = size * 0.48f

        // Stem
        val stemGradient = LinearGradient(
            x, baseY, x, baseY - stemHeight,
            intArrayOf(
                Color.HSVToColor(200, floatArrayOf(hue, 0.4f, 0.5f)),
                Color.HSVToColor(200, floatArrayOf(hue, 0.3f, 0.8f))
            ),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = stemGradient
        paint.style = Paint.Style.FILL
        val stemPath = Path().apply {
            moveTo(x - stemWidth * 0.6f, baseY)
            cubicTo(
                x - stemWidth * 0.8f, baseY - stemHeight * 0.3f,
                x - stemWidth * 0.5f, baseY - stemHeight * 0.7f,
                x - stemWidth * 0.4f, baseY - stemHeight
            )
            lineTo(x + stemWidth * 0.4f, baseY - stemHeight)
            cubicTo(
                x + stemWidth * 0.5f, baseY - stemHeight * 0.7f,
                x + stemWidth * 0.8f, baseY - stemHeight * 0.3f,
                x + stemWidth * 0.6f, baseY
            )
            close()
        }
        canvas.drawPath(stemPath, paint)

        // Decorative tendrils on stem
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size * 0.025f
        paint.color = Color.HSVToColor(120, floatArrayOf(hue, 0.5f, 0.9f))
        for (side in listOf(-1f, 1f)) {
            val tendrilPath = Path().apply {
                val tx = x + side * stemWidth * 0.5f
                val ty = baseY - stemHeight * 0.4f
                moveTo(tx, ty)
                cubicTo(
                    tx + side * size * 0.12f, ty - size * 0.05f,
                    tx + side * size * 0.15f, ty + size * 0.05f,
                    tx + side * size * 0.08f, ty + size * 0.1f
                )
            }
            canvas.drawPath(tendrilPath, paint)
        }

        // Cap
        val capY = baseY - stemHeight
        val capGradient = RadialGradient(
            x, capY - capRadius * 0.3f, capRadius * 1.2f,
            intArrayOf(
                Color.HSVToColor(255, floatArrayOf(hue, 0.7f, 1.0f)),
                Color.HSVToColor(255, floatArrayOf((hue + 20f) % 360f, 0.9f, 0.7f)),
                Color.HSVToColor(200, floatArrayOf((hue + 40f) % 360f, 1.0f, 0.4f))
            ),
            floatArrayOf(0f, 0.6f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = capGradient
        paint.style = Paint.Style.FILL
        val capPath = Path().apply {
            moveTo(x - capRadius * 1.1f, capY)
            cubicTo(
                x - capRadius * 1.1f, capY - capRadius * 0.5f,
                x - capRadius * 0.8f, capY - capRadius * 1.4f,
                x, capY - capRadius * 1.3f
            )
            cubicTo(
                x + capRadius * 0.8f, capY - capRadius * 1.4f,
                x + capRadius * 1.1f, capY - capRadius * 0.5f,
                x + capRadius * 1.1f, capY
            )
            cubicTo(
                x + capRadius * 0.8f, capY + capRadius * 0.25f,
                x - capRadius * 0.8f, capY + capRadius * 0.25f,
                x - capRadius * 1.1f, capY
            )
            close()
        }
        canvas.drawPath(capPath, paint)

        // Spot rings on cap
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Color.argb(180, 255, 255, 255)
        val spotPositions = listOf(
            Pair(0f, -0.7f), Pair(-0.45f, -0.4f), Pair(0.45f, -0.4f),
            Pair(-0.25f, -0.9f), Pair(0.25f, -0.9f)
        )
        for ((sx, sy) in spotPositions) {
            canvas.drawCircle(
                x + sx * capRadius,
                capY + sy * capRadius,
                size * 0.04f,
                paint
            )
        }

        // Cap glow
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size * 0.03f
        paint.color = Color.HSVToColor(100, floatArrayOf(hue, 0.8f, 1.0f))
        canvas.drawPath(capPath, paint)
        paint.shader = null
    }
}