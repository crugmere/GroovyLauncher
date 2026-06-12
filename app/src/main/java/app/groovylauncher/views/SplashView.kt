package app.groovylauncher.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import kotlin.math.*

class SplashView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var startTime = -1L
    private var isShowing = false
    private var currentBitmap: Bitmap? = null

    private data class Particle(
        var x: Float, var y: Float,
        var vx: Float, var vy: Float,
        var life: Float,
        var hue: Float,
        var size: Float
    )

    private val particles = mutableListOf<Particle>()

    // All instruments + Moss as easter egg
    private val instrumentResIds by lazy {
        listOf(
            app.groovylauncher.R.drawable.gold_top,
            app.groovylauncher.R.drawable.pearl,
            app.groovylauncher.R.drawable.roland,
            app.groovylauncher.R.drawable.bass,
            app.groovylauncher.R.drawable.jaguar,
            app.groovylauncher.R.drawable.cloud,
            app.groovylauncher.R.drawable.moss
        )
    }

    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (isShowing) {
                invalidate()
                choreographer.postFrameCallback(this)
            }
        }
    }

    fun show() {
        particles.clear()
        val resId = instrumentResIds.random()
        currentBitmap = BitmapFactory.decodeResource(resources, resId)
        startTime = System.currentTimeMillis()
        isShowing = true
        visibility = VISIBLE
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isShowing) return

        val elapsed = (System.currentTimeMillis() - startTime) / 1000f
        val cx = width / 2f
        val cy = height / 2f
        val fadeInDuration = 0.9f
        val holdDuration = 2.2f
        val dissolveDuration = 1.2f

        when {
            elapsed < fadeInDuration -> {
                val alpha = elapsed / fadeInDuration
                canvas.drawColor(Color.argb((alpha * 210).toInt().coerceIn(0, 210), 0, 0, 10))
                drawImage(canvas, cx, cy, alpha, elapsed)
            }
            elapsed < fadeInDuration + holdDuration -> {
                val holdTime = elapsed - fadeInDuration
                canvas.drawColor(Color.argb(210, 0, 0, 10))
                drawImage(canvas, cx, cy, 1f, holdTime)
            }
            elapsed < fadeInDuration + holdDuration + dissolveDuration -> {
                val dissolveTime = elapsed - fadeInDuration - holdDuration
                val progress = dissolveTime / dissolveDuration
                canvas.drawColor(Color.argb(((1f - progress) * 210).toInt().coerceIn(0, 210), 0, 0, 10))
                spawnParticles(cx, cy)
                drawImage(canvas, cx, cy, 1f - progress, dissolveTime)
                drawParticles(canvas)
            }
            else -> {
                isShowing = false
                visibility = GONE
                choreographer.removeFrameCallback(frameCallback)
            }
        }
    }

    private fun drawImage(canvas: Canvas, cx: Float, cy: Float, alpha: Float, t: Float) {
        val bitmap = currentBitmap ?: return
        val a = (alpha * 255).toInt().coerceIn(0, 255)
        val bob = sin(t * 2.5f) * 12f

        val maxSize = minOf(width, height) * 0.78f
        val scale = maxSize / maxOf(bitmap.width, bitmap.height)
        val bw = bitmap.width * scale
        val bh = bitmap.height * scale

        paint.alpha = a
        canvas.drawBitmap(
            bitmap,
            Rect(0, 0, bitmap.width, bitmap.height),
            RectF(cx - bw / 2f, cy - bh / 2f + bob, cx + bw / 2f, cy + bh / 2f + bob),
            paint
        )
        paint.alpha = 255
    }

    private fun spawnParticles(cx: Float, cy: Float) {
        if (particles.size < 100 && Math.random() < 0.4) {
            val angle = (Math.random() * 2 * PI).toFloat()
            val speed = (Math.random() * 4 + 1).toFloat()
            particles.add(Particle(
                x = cx + (Math.random() * 260 - 130).toFloat(),
                y = cy + (Math.random() * 380 - 190).toFloat(),
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 1.5f,
                life = 1f,
                hue = (Math.random() * 120 + 260).toFloat(),
                size = (Math.random() * 14 + 5).toFloat()
            ))
        }
        particles.forEach { p ->
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.06f
            p.life -= 0.018f
        }
        particles.removeAll { it.life <= 0 }
    }

    private fun drawParticles(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.shader = null
        for (p in particles) {
            val a = (p.life * 255).toInt().coerceIn(0, 255)
            paint.color = Color.HSVToColor(a, floatArrayOf(p.hue % 360f, 0.9f, 1.0f))
            canvas.drawCircle(p.x, p.y, p.size * p.life, paint)
        }
    }
}