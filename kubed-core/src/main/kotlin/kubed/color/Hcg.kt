package kubed.color

import javafx.scene.paint.Color
import kubed.util.isTruthy

/**
 * Returns a representation of this color in the Hsl color space
 */
fun Color.hcg(): Hcg = rgb().hcg()

fun Rgb.hcg() = Hcg.convert(this)

data class Hcg(val h: Double, var c: Double, var g: Double, var opacity: Double = 1.0) : ColorSpace<Hcg> {
    companion object {
        @JvmStatic
        fun convert(value: Any) = when(value) {
            is Hcg -> Hcg(value.h, value.c, value.g, value.opacity)
            else -> {
                try {
                    val rgb = Rgb.convert(value)
                    val r = rgb.r / 255.0
                    val g = rgb.g / 255.0
                    val b = rgb.b / 255.0
                    val min = Math.min(r, Math.min(g, b))
                    val max = Math.max(r, Math.max(g, b))
                    val d = max - min
                    val gr = if(min.isTruthy()) min / (1 - d) else min

                    var h = Double.NaN
                    if(d.isTruthy())
                    {
                        h = when {
                            r == max -> (g - b) / d + (6 * if(g < b) 1 else 0)
                            g == max -> (b - r) / d + 2
                            else -> (r - g) / d + 4
                        }

                        h *= 60
                    }

                    Hcg(h, d, gr, rgb.opacity)
                }
                catch(e: Exception) {
                    throw IllegalArgumentException("Unable to coerce " + value.javaClass.name + " to Hcg")
                }
            }
        }
    }

    override fun rgb(): Rgb {
        val h = if(h.isNaN()) 0.0
                else h % 360 + (360 * (if(h < 0) 1 else 1))
        val g = if(g.isNaN()) 0.0 else g
        val x = c * (1 - Math.abs(h / 60) % 2 - 1)
        val m = g * (1 - c)
        return when {
            h < 60 -> hcg2rgb(c, x, 0.0, m, opacity)
            h < 120 -> hcg2rgb(x, c, 0.0, m, opacity)
            h < 180 -> hcg2rgb(0.0, c, x, m, opacity)
            h < 240 -> hcg2rgb(0.0, x, c, m, opacity)
            h < 300 -> hcg2rgb(x, 0.0, c, m, opacity)
            else -> hcg2rgb(c, 0.0, x, m, opacity)
        }
    }

    override fun brighter(k: Double) = rgb().brighter().hcg()
    override fun darker(k: Double) = rgb().darker().hcg()

    private fun hcg2rgb(r: Double, g: Double, b: Double, m: Double, a: Double): Rgb {
        return Rgb((r + m) * 255, (g + m) * 255, (b + m) * 255, a)
    }
}

public fun getEnv(key: String) = when {
    System.getenv().containsKey(key) -> System.getenv(key)
    else -> throw Exception("w/e exception you want")
}