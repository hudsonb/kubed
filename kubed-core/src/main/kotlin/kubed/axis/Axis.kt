package kubed.axis

import com.sun.javafx.css.Size
import com.sun.javafx.css.SizeUnits
import javafx.geometry.Side
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.HLineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.VLineTo
import javafx.scene.transform.Translate
import kubed.scale.BandScale
import kubed.scale.Scale
import kubed.selection.Selection
import kubed.shape.TextAnchor
import kubed.shape.lineSegment
import kubed.shape.text

class Axis<D, R: Number>(val side: Side, val scale: Scale<D, R>) {
    var tickSizeInner = 6.0
    var tickSizeOuter = 6.0
    var tickPadding = 3.0
    var formatter: (D) -> String = { d -> d.toString() }
    var tickCount = 10
    var tickValues: List<D>? = null

    val transform: (Double) -> Translate = when(side) {
        Side.TOP, Side.BOTTOM -> { x -> Translate(x, 0.0) }
        else -> { y -> Translate(0.0, y) }
    }

    private val k = when(side) {
        Side.TOP, Side.LEFT -> -1
        else -> 1
    }

    fun tickSize(size: Double): Axis<D, R> {
        tickSizeInner = size
        tickSizeOuter = size
        return this
    }

    fun tickSizeInner(size: Double): Axis<D, R> {
        tickSizeInner = size
        return this
    }

    fun tickSizeOuter(size: Double): Axis<D, R> {
        tickSizeOuter = size
        return this
    }

    fun tickPadding(padding: Double): Axis<D, R> {
        tickPadding = padding
        return this
    }

    fun tickCount(count: Int): Axis<D, R> {
        tickCount = count
        return this
    }

    fun tickValues(values: List<D>?): Axis<D, R> {
        tickValues = values
        return this
    }

    fun formatter(format: (D) -> String): Axis<D, R> {
        formatter = format
        return this
    }

    private fun center(scale: BandScale<D>): (d: D) -> R {
        var offset = scale.bandwidth / 2.0
        if(scale.round)
            offset = Math.round(offset).toDouble()
        return { d -> (scale(d) + offset) as R }
    }

    private fun identity(scale: Scale<D, R>): (d: D) -> R = {
        d ->
        scale(d)
    }

    operator fun invoke(sel: Selection) {
        val values = ArrayList<D>()
        if(tickValues == null)
        {
            values.addAll(scale.ticks(tickCount))
        }
        else values += tickValues!!

        if(values.isEmpty())
            throw IllegalStateException("Failed to determine tick values")

        val spacing = Math.max(tickSizeInner, 0.0) + tickPadding
        val range = scale.range
        val range0 = range.first().toDouble() + 0.5
        val range1 = range.last().toDouble() + 0.5
        val position = if(scale is BandScale) center(scale) else identity(scale) // TODO: Pass copy of scale
        var path = sel.selectAll(".domain").data(listOf(listOf(Unit))) // Well this is hideous
        var tick = sel.selectAll(".tick").data(listOf(values as List<Any>), { d, _, _ -> scale(d as D) })
        val tickExit = tick.exit()
        val tickEnter = tick.enter().append(fun(): Node {
            val g = Group()
            g.styleClass += "tick"
            return g
        })
        var line = tick.select("line")
        var text = tick.select("text")

        
        path = path.merge(path.enter().append(fun(): Node { return Path() })
                                    .classed("domain")
                                    .stroke(Color.BLACK))

        tick = tick.merge(tickEnter)

        val lineSegment = lineSegment<Unit> {
            startX {
                when(side) {
                    Side.LEFT, Side.RIGHT -> 0.0
                    else -> 0.5
                }
            }

            startY {
                when(side) {
                    Side.TOP, Side.BOTTOM -> 0.0
                    else -> 0.5
                }
            }

            endX {
                when(side) {
                    Side.LEFT, Side.RIGHT -> k * tickSizeInner
                    else -> 0.5
                }
            }

            endY {
                when(side) {
                    Side.TOP, Side.BOTTOM -> k * tickSizeInner
                    else -> 0.5
                }
            }

            stroke(Color.ORANGERED)
        }

        line = line.merge(tickEnter.append { _, _, _-> lineSegment(Unit)})

        val label = text<D> {
            text { d -> formatter(d) }
            textAnchor { TextAnchor.MIDDLE }
            fill(Color.BLACK)
            x {
                when (side) {
                    Side.LEFT, Side.RIGHT -> k * spacing
                    else -> 0.5
                }
            }
            y {
                when (side) {
                    Side.TOP, Side.BOTTOM -> k * spacing
                    else -> 0.5
                }
            }
            translateY {
                when (side) {
                    Side.TOP -> 0.0
                    Side.BOTTOM -> Size(0.71, SizeUnits.EM).pixels()
                    else -> Size(0.32, SizeUnits.EM).pixels()
                }
            }
        }

        text = text.merge(tickEnter.append { d, _, _ -> label(d as D) })

        tickExit.remove()

        path.forEach<Path> { _, _, _ ->
            when(side) {
                Side.LEFT, Side.RIGHT -> {
                    elements += MoveTo(k * tickSizeOuter, range0)
                    elements += HLineTo(0.5)
                    elements += VLineTo(range1)
                    elements += HLineTo(k * tickSizeOuter)
                }
                else -> {
                    elements += MoveTo(range0, k * tickSizeOuter)
                    elements += VLineTo(0.5)
                    elements += HLineTo(range1)
                    elements += VLineTo(k * tickSizeOuter)
                }
            }
        }

        tick.opacity(1.0)
                .transform { d, _, _ -> listOf(transform(position(d as D).toDouble())) }
    }
}
