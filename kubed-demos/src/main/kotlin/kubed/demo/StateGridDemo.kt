package kubed.demo

import javafx.application.Application
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.VPos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Color.color
import javafx.stage.Stage
import kubed.color.hcl
import kubed.color.rgb
import kubed.interpolate.interpolateHcl
import kubed.scale.LinearScale
import kubed.selection.selectAll
import kubed.shape.TextAnchor
import kubed.shape.rect
import kubed.shape.text
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class StateGridDemo: Application() {
    override fun start(primaryStage: Stage) {
        val width = 960.0
        val height = 500.0
        val cellSize = 40.0

        val root = StackPane()

        val g = Group()
        root.children += g

        //val color = LinearScale(::interpolateHcl)
        //color.domain(listOf(0.0, 1.0))
        //color.range(listOf(Color.web("#f7fcb9").rgb().hcl(), Color.web("#31a354").rgb().hcl()))

        val rect = rect<State> {
            layoutX(-cellSize / 2)
            layoutY(-cellSize / 2)
            translateX { d, _ -> d.col * cellSize }
            translateY { d, _ -> d.row * cellSize }
            width(cellSize - 2)
            height(cellSize - 2)
            //fill { d, _ -> color(d.value).toColor() }
            fill { d, _ -> if(d.party == "d") Color.web("#2196F3") else Color.web("#FF5252") }
            //stroke { d, _ -> if(d.battleground) Color.YELLOW else null }
        }

        val text = text<State> {
            text { d, _ -> d.abbrv }
            translateX { d, _ -> d.col * cellSize }
            translateY { d, _ -> d.row * cellSize }
            textAnchor(TextAnchor.MIDDLE)
            textOrigin(VPos.CENTER)
        }

        val states = g.selectAll<State>("Rectangle")
                         .data(data())
                         .enter()
        states.append { d, _, _ -> rect(d) }
        states.append { d, _, _ -> text(d) }


        val scene = Scene(root)

        saveAsPng(root)

        primaryStage.scene = scene
        primaryStage.width = width
        primaryStage.height = height
        primaryStage.show()
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

    data class State(val abbrv: String, val row: Int, val col: Int, val party: String, val battleground: Boolean = false)
    fun data(): List<State> {
        return listOf(State("ME", 0, 10, "d"),
                      State("WI", 1, 5, "r", true), State("VT", 1, 9, "d"), State("NH", 1, 10, "d", true),
                      State("WA", 2, 0, "d"), State("ID", 2, 1, "r", true), State("MT", 2, 2, "r"), State("ND", 2, 3, "r"), State("MN", 2, 4, "d"), State("IL", 2, 5, "d"), State("MI", 2, 6, "r", true), State("NY", 2, 8, "d"), State("MA", 2, 9, "d"),
                      State("OR", 3, 0, "d"), State("NV", 3, 1, "d", true), State("WY", 3, 2, "r"), State("SD", 3, 3, "r"), State("IA", 3, 4, "r", true), State("IN", 3, 5, "r"), State("OH", 3, 6, "r", true), State("PA", 3, 7, "r", true), State("NJ", 3, 8, "d"), State("CT", 3, 9, "d"), State("RI", 3, 10, "d"),
                      State("CA", 4, 0, "d"), State("UT", 4, 1, "r", true), State("CO", 4, 2, "d"), State("NE", 4, 3, "r"), State("MO", 4, 4, "r"), State("KY", 4, 5, "r"), State("WV", 4, 6, "r"), State("VA", 4, 7, "d", true), State("MD", 4, 8, "d"), State("DE", 4, 9, "d"),
                      State("AZ", 5, 1, "r", true), State("NM", 5, 2, "d"), State("KS", 5, 3, "r"), State("AR", 5, 4, "r"), State("TN", 5, 5, "r"), State("NC", 5, 6, "r", true), State("SC", 5, 7, "r"),
                      State("OK", 6, 3, "r"), State("LA", 6, 4, "r"), State("MS", 6, 5, "r"), State("AL", 6, 6, "r"), State("GA", 6, 7, "r"),
                      State("HI", 7, 0, "d"), State("AK", 7, 1, "r"), State("TX", 7, 3, "r"), State("FL", 7, 8, "r", true))
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(StateGridDemo::class.java, *args)
        }
    }
}