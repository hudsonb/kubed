package kubed.demo

import javafx.application.Application
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import kubed.ScalingPane
import kubed.layout.chord.Chord
import kubed.layout.chord.ChordDiagram
import kubed.layout.chord.Chords
import kubed.layout.chord.Ribbon
import kubed.scale.scaleOrdinal
import kubed.shape.arc

import javafx.scene.Group
import javafx.scene.paint.Color
import kubed.selection.selectAll
import java.util.Comparator
import kotlin.math.min

class ChordDiagramDemo: Application() {
    override fun start(primaryStage: Stage?) {
        val matrix = listOf(listOf(0.0, 0.0, 0.0, 10.0, 5.0, 15.0),
                                             listOf(0.0, 0.0, 0.0, 5.0, 15.0, 20.0),
                                             listOf(0.0, 0.0, 0.0, 15.0, 5.0, 5.0),
                                             listOf(10.0, 5.0, 15.0, 0.0, 0.0, 0.0),
                                             listOf(5.0, 15.0, 5.0, 0.0, 0.0, 0.0),
                                             listOf(15.0, 20.0, 5.0, 0.0, 0.0, 0.0))
        val width = 960.0
        val height = 960.0
        val outerRadius = min(width, height) * 0.5 - 40
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
            range(listOf(Color.SLATEGRAY, Color.SLATEGRAY, Color.SLATEGRAY, Color.rgb(237, 200, 80),
                    Color.rgb(203, 50, 62), Color.rgb(0, 160, 175)))
        }

        val arc = arc<kubed.layout.chord.Group> {
            startAngle { d, _ -> d.startAngle }
            endAngle { d, _ -> d.endAngle }
            fill { _, i -> color(i) }

            stroke(null)
            outerRadius(outerRadius)
            innerRadius(innerRadius)
        }

        val ribbon = Ribbon().apply {
            radius = { _, _ -> innerRadius }
            fill { d, _ -> color(d.target.index) }

            stroke(null)
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

        g.append { -> Group().apply { styleClass += "ribbons" } }
                .selectAll<Chord>("Path")
                .data<Chords>({ d, _, _ -> d!! })
                .enter()
                .append { d, i, _ -> ribbon(d, i) }

        val sp = ScalingPane()
        sp.contentPane = StackPane(root)
        sp.prefWidth = 600.0
        sp.prefHeight = 600.0
        val scene = Scene(sp)
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(ChordDiagramDemo::class.java, *args)
        }
    }
}