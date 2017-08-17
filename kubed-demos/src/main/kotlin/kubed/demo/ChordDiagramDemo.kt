package kubed.demo

import javafx.application.Application
import javafx.application.Application.launch
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.paint.Color
import javafx.scene.paint.Color.color
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.transform.Rotate
import javafx.scene.transform.Translate
import javafx.stage.Stage
import kubed.layout.chord.Chord
import kubed.layout.chord.ChordDiagram
import kubed.layout.chord.Chords
import kubed.layout.chord.Ribbon
import kubed.scale.scaleOrdinal
import kubed.selection.selectAll
import kubed.shape.arc
import kubed.shape.lineSegment
import kubed.shape.rect
import sun.text.normalizer.UTF16.append
import java.io.File
import java.io.IOException
import java.util.Comparator
import javax.imageio.ImageIO
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus

class ChordDiagramDemo: Application() {
    override fun start(primaryStage: Stage?) {
//        val matrix = listOf(listOf(11975.0,  5871.0, 8916.0, 2868.0),
//                            listOf(1951.0, 10048.0, 2060.0, 6171.0),
//                            listOf(8010.0, 16145.0, 8090.0, 8045.0),
//                            listOf(1013.0,   990.0,  940.0, 6907.0))

        val matrix = listOf(listOf(0.0, 0.0, 0.0, 10.0, 5.0, 15.0),
                            listOf(0.0, 0.0, 0.0, 5.0, 15.0, 20.0),
                            listOf(0.0, 0.0, 0.0, 15.0, 5.0, 5.0),
                            listOf(10.0, 5.0, 15.0, 0.0, 0.0, 0.0),
                            listOf(5.0, 15.0, 5.0, 0.0, 0.0, 0.0),
                            listOf(15.0, 20.0, 5.0, 0.0, 0.0, 0.0))
        val width = 960.0
        val height = 960.0
        val outerRadius = Math.min(width, height) * 0.5 - 40
        val innerRadius = outerRadius - 30

        val root = Group()
        root.prefWidth(width)
        root.prefHeight(height)

        val chord = ChordDiagram().apply {
            padAngle = 0.05
            subgroupComparator = Comparator.reverseOrder()
        }

        val color = scaleOrdinal<Int, Color> {
            domain((0 until 6).toList())
            range(listOf(Color.GRAY, Color.GRAY, Color.GRAY, Color.rgb(237, 200, 80), Color.rgb(203, 50, 62), Color.rgb(0, 160, 175)))
        }

        val arc = arc<kubed.layout.chord.Group> {
            startAngle { d, _ -> d.startAngle }
            endAngle { d, _ -> d.endAngle }
            fill { _, i -> color(i) }
            //stroke { _, i -> color(i).darker() }
            stroke(Color.TRANSPARENT)

            outerRadius(outerRadius)
            innerRadius(innerRadius)
        }

        val ribbon = Ribbon().apply {
            radius = { _, _ -> innerRadius }
            fill { d, _ -> color(d.target.index) }
            //stroke { d, _ -> color(d.target.index).darker() }
            stroke(Color.TRANSPARENT)
            opacity(0.55)
        }

        val g = root.selectAll<Chords>()
                    .append { -> Group() }
                    .datum(chord.chord(matrix))

        val group = g.append { -> Group() }
                .selectAll<kubed.layout.chord.Group?>("Group")
                .data<Chords>({ d: Chords?, _, _ -> d?.groups!! })
                .enter()
                .append { _, _, _ -> Group() }

        group.append { d, i, _ -> arc(d!!, i)}



//        val groupTick = group.selectAll<Tick>(".group-tick")
//                             .data<kubed.layout.chord.Group>({ d, _, _ -> groupTicks(d!!, 1e3) })
//                             .enter()
//                             .append { _, _, _ -> Group() }
//                             .styleClasses { _, _, _ -> listOf("group-tick") }
//                             .transform { d, _, _ -> listOf(Translate(outerRadius * Math.cos(d.angle), outerRadius * Math.sin(d.angle)), Rotate(d.angle * 180 / Math.PI))}
//
//        groupTick.append { -> Line(0.0, 0.0, 6.0, 0.0) }

        g.append { -> Group().apply { styleClass += "ribbons" } }
                .selectAll<Chord>("Path")
                .data<Chords>({ d, _, _ -> d!! })
                .enter()
                .append { d, i, _ -> ribbon(d, i) }


        saveAsPng(root)

        val scene = Scene(root)
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    data class Tick(val value: Double, val angle: Double)

    fun groupTicks(d: kubed.layout.chord.Group, step: Double): List<Tick> {
        val k = (d.endAngle - d.startAngle) / d.value

        val ticks = ArrayList<Tick>()
        var value = 0.0
        while(value < d.value) {
            ticks += Tick(value, value * k + d.startAngle)
            value += step
        }

        return ticks
    }

    fun saveAsPng(node: Node) {
        val image = node.snapshot(SnapshotParameters(), null)

        // TODO: probably use a file chooser here
        val file = File("chart.png")

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file)
        } catch (e: IOException) {
            // TODO: handle exception here
        }
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(ChordDiagramDemo::class.java, *args)
        }
    }
}