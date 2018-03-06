package kubed.geo.tile

import javafx.geometry.Rectangle2D
import kubed.array.range
import java.lang.Math.pow
import kotlin.math.*

fun tile(init: Tiler.() -> Unit) = Tiler().apply(init)

private val ln2 = ln(2.0)

data class Tile(val x: Int, val y: Int, val z: Int, val tx: Double, val ty: Double)
data class TileSet(val translateX: Double, val translateY: Double, val scale: Double) {
    val tiles = ArrayList<Tile>()
}

class Tiler {
    var extent: Rectangle2D = Rectangle2D(0.0, 0.0, 960.0, 500.0)
    var translateX = (extent.minX + extent.maxX) / 2
    var translateY = (extent.minY + extent.maxY) / 2
    var tileSize = 256
    var scale = 256.0
    var zoomDelta = 0.0
    var wrap = true

    operator fun invoke(): TileSet {
        val log2tileSize = ln(tileSize.toDouble()) / ln2
        val z = max(ln(scale) / ln2 - log2tileSize, 0.0)
        val z0 = (z + zoomDelta).roundToInt()
        val j = 1 shl z0
        val k = pow(2.0, z - z0 + log2tileSize)
        val x = translateX - scale / 2
        val y = translateY - scale / 2

        val tileSet = TileSet(x / k, y / k, k)

        val cols = range(max(if(wrap) Double.NEGATIVE_INFINITY else 0.0, floor((extent.minX - x) / k)),
                                     min(ceil((extent.maxX - x) / k), if(wrap) Double.POSITIVE_INFINITY else j.toDouble()))

        val rows = range(max(0.0, floor((extent.minY - y) / k)),
                                     min(ceil((extent.maxY - y) / k), j.toDouble()))

        rows.forEach { r ->
            cols.forEach { c ->
                tileSet.tiles += Tile(((c % j + j) % j).toInt(), r.toInt(), z0, c * tileSize, r * tileSize)
            }
        }

        return tileSet
    }
}