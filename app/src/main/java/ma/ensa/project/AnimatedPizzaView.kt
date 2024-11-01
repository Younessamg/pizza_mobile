package ma.ensa.project

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class AnimatedPizzaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pizzaBaseDrawable: Drawable? = null
    private var toppingBitmaps: MutableMap<String, Bitmap> = mutableMapOf()
    private var currentScale = 1f
    private var selectedToppings = mutableSetOf<String>()
    private var toppingAnimations: MutableMap<String, Float> = mutableMapOf()
    private val toppingPositions: MutableMap<String, List<ToppingPosition>> = mutableMapOf()
    private val paint = Paint().apply { isAntiAlias = true }
    private val matrix = Matrix()

    data class ToppingPosition(
        val x: Float,
        val y: Float,
        val rotation: Float,
        val scale: Float
    )

    private var targetScale = 1f
    private val scaleAnimator = ValueAnimator().apply {
        duration = 300
        interpolator = DecelerateInterpolator()
        addUpdateListener { animation ->
            currentScale = animation.animatedValue as Float
            invalidate()
        }
    }

    init {
        // Load pizza base drawable
        pizzaBaseDrawable = ContextCompat.getDrawable(context, R.drawable.pizza_base)
    }

    fun setSize(size: String) {
        targetScale = when (size) {
            "small" -> 0.8f
            "medium" -> 1f
            "large" -> 1.2f
            else -> 1f
        }

        scaleAnimator.cancel()
        scaleAnimator.setFloatValues(currentScale, targetScale)
        scaleAnimator.start()
    }
    fun toggleTopping(toppingName: String, isChecked: Boolean) {
        if (isChecked && !selectedToppings.contains(toppingName)) {
            selectedToppings.add(toppingName)
            if (!toppingPositions.containsKey(toppingName)) {
                generateToppingPositions(toppingName)
            }
            animateTopping(toppingName, true)
        } else if (!isChecked && selectedToppings.contains(toppingName)) {
            selectedToppings.remove(toppingName)
            animateTopping(toppingName, false)
        }
    }

    private fun generateToppingPositions(toppingName: String) {
        val positions = mutableListOf<ToppingPosition>()
        val random = Random(toppingName.hashCode())

        val count = when (toppingName) {
            "pepperoni" -> 12
            "mushrooms" -> 8
            "onions" -> 10
            "sausage" -> 8
            "extra_cheese" -> 15
            else -> 10
        }

        // Define the radius range for more centered distribution
        val minRadius = 0.1f  // Minimum distance from center (increased from 0)
        val maxRadius = 0.6f  // Maximum distance from center (reduced from 0.8)

        for (i in 0 until count) {
            // Modified spiral pattern for more centered distribution
            val angle = i * 137.5f * (Math.PI / 180f) // Golden angle

            // Adjust radius calculation for more central distribution
            val normalizedIndex = i.toFloat() / count
            val radiusMultiplier = if (normalizedIndex < 0.3f) {
                // Inner circle (30% of toppings)
                minRadius + (normalizedIndex * 2f * (maxRadius - minRadius) / 3f)
            } else {
                // Outer circle (70% of toppings)
                minRadius + ((normalizedIndex - 0.3f) * (maxRadius - minRadius))
            }

            val radius = radiusMultiplier * (1f + random.nextFloat() * 0.2f - 0.1f) // Add slight randomness

            // Convert to Cartesian coordinates with center offset
            val x = 0.5f + (radius * cos(angle)).toFloat()
            val y = 0.5f + (radius * sin(angle)).toFloat()

            // Adjust scale based on distance from center for more natural look
            val distanceFromCenter = kotlin.math.sqrt((x - 0.5f) * (x - 0.5f) + (y - 0.5f) * (y - 0.5f))
            val baseScale = when (toppingName) {
                "pepperoni" -> 0.15f to 0.25f
                "mushrooms" -> 0.2f to 0.3f
                "onions" -> 0.15f to 0.23f
                "sausage" -> 0.18f to 0.3f
                "extra_cheese" -> 0.25f to 0.4f
                else -> 0.2f to 0.3f
            }

            // Scale slightly smaller towards edges for perspective
            val scaleMultiplier = 1f - (distanceFromCenter * 0.2f)
            val scale = (baseScale.first + random.nextFloat() * (baseScale.second - baseScale.first)) * scaleMultiplier

            // Randomize rotation based on position
            val rotation = random.nextFloat() * 360f

            positions.add(ToppingPosition(x, y, rotation, scale))
        }

        toppingPositions[toppingName] = positions
    }
    private fun animateTopping(toppingName: String, show: Boolean) {
        val animator = ValueAnimator.ofFloat(
            toppingAnimations[toppingName] ?: if (show) 0f else 1f,
            if (show) 1f else 0f
        ).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                toppingAnimations[toppingName] = animation.animatedValue as Float
                invalidate()
            }
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val baseSize = min(width, height).toFloat()
        val scaledSize = baseSize * currentScale

        // Draw pizza base
        pizzaBaseDrawable?.let { drawable ->
            val left = (centerX - scaledSize / 2).toInt()
            val top = (centerY - scaledSize / 2).toInt()
            val right = (centerX + scaledSize / 2).toInt()
            val bottom = (centerY + scaledSize / 2).toInt()

            drawable.setBounds(left, top, right, bottom)
            drawable.draw(canvas)
        }

        // Draw toppings
        selectedToppings.forEach { toppingName ->
            val bitmap = toppingBitmaps[toppingName] ?: return@forEach
            val alpha = ((toppingAnimations[toppingName] ?: 1f) * 255).toInt()
            paint.alpha = alpha

            toppingPositions[toppingName]?.forEach { position ->
                matrix.reset()

                // Calculate actual position based on pizza size
                val actualX = centerX - scaledSize / 2 + (scaledSize * position.x)
                val actualY = centerY - scaledSize / 2 + (scaledSize * position.y)

                // Set up matrix transformations
                matrix.postTranslate(-bitmap.width / 2f, -bitmap.height / 2f)
                matrix.postRotate(position.rotation)
                matrix.postScale(position.scale * currentScale, position.scale * currentScale)
                matrix.postTranslate(actualX, actualY)

                // Draw the topping
                canvas.drawBitmap(bitmap, matrix, paint)
            }
        }
    }

    fun loadToppingDrawable(toppingName: String, drawableResId: Int) {
        val drawable = ContextCompat.getDrawable(context, drawableResId) ?: return
        if (drawable is BitmapDrawable) {
            toppingBitmaps[toppingName] = drawable.bitmap
        } else {
            // Convert drawable to bitmap if it's not already
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            toppingBitmaps[toppingName] = bitmap
        }
    }

}