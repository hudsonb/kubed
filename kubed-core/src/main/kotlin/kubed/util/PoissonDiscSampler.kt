package kubed.util

import javafx.geometry.Point2D
import java.lang.Math.random
import java.util.*
import kotlin.math.*

/**
 * A Poisson Disc sampler based on [Jason Davies' implementation](https://www.jasondavies.com/poisson-disc/).
 *
 * Produces points which are tightly-packed, but no closer to one another than a specified minimum distance.
 */
class PoissonDiscSampler(val width: Double, val height: Double, val radius: Double) {
    private val k = 30
    private val radius2 = radius * radius
    private val R = 3 * radius2
    private val cellSize = radius * Math.sqrt(.5)
    private val gridWidth = ceil(width / cellSize).toInt()
    private val gridHeight = ceil(height / cellSize).toInt()
    private val grid = Array<Point2D?>(gridWidth * gridHeight) { null }
    private var queue = ArrayList<Point2D>()
    private var sampleSize = 0

    operator fun invoke(): Point2D? {
        if(sampleSize == 0) return sample(random() * width, random() * height)

        while(queue.isNotEmpty()) {
            val i = (random() * queue.size).toInt()
            val s= queue[i]

            for(j in 0 until k) {
                val a = 2.0 * PI * random()
                val r = sqrt(random() * R + radius2)
                val x = s.x + r * cos(a)
                val y = s.y + r * sin(a)

                // Reject candidates that are outside the allowed extent, or closer than 2 * radius to any existing sample.
                if(0 <= x && x < width && 0 <= y && y < height && far(x, y)) return sample(x, y)
            }

            queue[i] = queue.last()
            queue.removeAt(queue.lastIndex)
        }

        return null
    }

    private fun sample(x: Double, y: Double): Point2D {
        val s = Point2D(x, y)
        queue.add(s)
        grid[gridWidth * (y / cellSize).toInt() + (x / cellSize).toInt()] = s
        ++sampleSize
        return s
    }

    private fun far(x: Double, y: Double): Boolean {
        val i = (x / cellSize).toInt()
        val j = (y / cellSize).toInt()
        val i0 = max(i - 2.0, 0.0).toInt()
        val j0 = max(j - 2.0, 0.0).toInt()
        val i1 = min(i + 3.0, gridWidth.toDouble()).toInt()
        val j1 = min(j + 3.0, gridHeight.toDouble()).toInt()

        for(j in j0 until j1) {
            val o = j * gridWidth
            for(i in i0 until i1) {
                val s = grid[o + i]
                if(s != null) {
                    val dx = s.x - x
                    val dy = s.y - y
                    if(dx * dx + dy * dy < radius2) return false
                }
            }
        }

        return true
    }
}