package kubed.axis


import javafx.geometry.Side
import javafx.geometry.VPos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.paint.Color
import javafx.scene.shape.*
import javafx.scene.text.Font
import javafx.scene.transform.Translate
import kubed.scale.BandScale
import kubed.scale.Scale
import kubed.selection.Selection
import kubed.shape.TextAnchor
import kubed.shape.lineSegment
import kubed.shape.text
import snap
import java.util.*
import kotlin.math.max
import kotlin.math.round

class Axis<D, R: Number>(val side: Side, val scale: Scale<D, R>) {
    var tickSizeInner = 6.0
    var tickSizeOuter = 6.0
    var tickPadding = 3.0
    var formatter: (D) -> String = { d -> d.toString() }
    var tickCount = 10
    var tickValues: List<D>? = null

    val transform: (Double) -> Translate = when(side) {
        Side.TOP, Side.BOTTOM -> { x -> Translate(snap(x), 0.0) }
        else -> { y -> Translate(0.0, snap(y)) }
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
            offset = round(offset).toDouble()
        return { d -> (scale(d) + offset) as R }
    }

    private fun identity(scale: Scale<D, R>): (d: D) -> R = {
        d ->
        scale(d)
    }

    operator fun <T> invoke(sel: Selection<T>) {
        val values = ArrayList<D>()
        if(tickValues == null)
        {
            values.addAll(scale.ticks(tickCount))
        }
        else values += tickValues!!

        if(values.isEmpty())
            throw IllegalStateException("Failed to determine tick values")

        val spacing = max(tickSizeInner, 0.0) + tickPadding
        val range = scale.range
        val range0 = range.first().toDouble() + 0.5
        val range1 = range.last().toDouble() + 0.5
        val position = if(scale is BandScale) center(scale) else identity(scale) // TODO: Pass copy of scale
        var path = sel.selectAll<Unit>(".domain").data(listOf(Unit)) // Well this is hideous
        var tick = sel.selectAll<D>(".tick").data(values, { d, _, _ -> scale(d) })
        val tickExit = tick.exit()
        val tickEnter = tick.enter().append(fun(): Node {
            val g = Group()
            g.styleClass += "tick"
            return g
        })
        var line = tick.select("line")
        var text = tick.select("text")


        path = path.merge(path.enter().append { -> Path() }
                                      .classed("domain")
                                      .stroke(Color.BLACK))

        tick = tick.merge(tickEnter)

        val vertical = side == Side.LEFT || side == Side.RIGHT
        val lineSegment = lineSegment<Unit> {
            startX(0.5)
            startY(0.5)
            endX(if(vertical) k * tickSizeInner else 0.5)
            endY(if(vertical) 0.5 else k * tickSizeInner)
            strokeLineCap(StrokeLineCap.SQUARE)
            smooth(false)
            stroke(Color.BLACK)
        }

        line = line.merge(tickEnter.append { _, _, _-> lineSegment(Unit)})

        val label = text<D> {
            font(Font("Courier New", 10.0))
            text { d, _ -> formatter(d) }
            textAnchor { _, _ -> if(vertical) TextAnchor.END else TextAnchor.MIDDLE }
            textOrigin { _, _ -> if(vertical) VPos.CENTER else VPos.TOP }
            fill(Color.BLACK)
            x(if(vertical) k * spacing else 0.5)
            y(if(vertical) 0.5 else k * spacing)
        }

        text = text.merge(tickEnter.append { d, _, _ -> label(d) })

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
            .transform { d, _, _ -> listOf(transform(position(d).toDouble())) }
    }
}
