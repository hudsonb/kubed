package kubed.demo

import javafx.animation.Animation
import javafx.animation.Interpolator
import javafx.animation.Timeline
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Duration
import kubed.interpolate.interpolateRgb
import kubed.selection.selectAll
import kubed.shape.rect
import kubed.transition.active
import kubed.transition.transition

private data class Cell(val row: Int, val col: Int) {}

class CyclingTransitionIIDemo : Application() {
    val n = 4002.0

    override fun start(primaryStage: Stage?) {
        val margin = 10.0

        val whiteblue = interpolateRgb("#eee", "steelblue")
        val blueorange = interpolateRgb("steelblue", "orange")
        val orangewhite = interpolateRgb("orange", "#eee")

        //Thread.sleep(30000);

        val data = ArrayList<Cell>()
        for(r in 0 until 40) {
            for(c in 0 until 60)
                data += Cell(r, c)
        }

        val root = Group()
        //root.translateX = margin
        //root.translateY = margin

        val width = 60 * 10.0 + margin * 2
        val height = 40 * 10.0 + margin * 2

        val r = rect<Cell>().height(10.0)
                            .width(10.0)
                            .translateX { (_, col) -> col * 11.0 + margin / 2 }
                            .translateY { (row, _) -> row * 11.0 + margin / 2 }
                            .stroke(Color.TRANSPARENT)
                            .fill(Color.LIGHTGREY)

        root.selectAll<Node>()
            .data(listOf(data))
            .enter().append { d, _, _ -> r(d as Cell) }
            .transition()
            .interpolator(Interpolator.LINEAR)
            .delay { _, i, _ -> Duration.millis(i + Math.random() * n / 4.0) }
            .on(Animation.Status.RUNNING) { repeat(this) }

        val scene = Scene(root)
        primaryStage?.width = width + margin * 2
        primaryStage?.height = height + margin * 2

        primaryStage?.scene = scene
        primaryStage?.show()
    }

    fun repeat(node: Node) {
        //println("repeat")
        node.active()?.fill(Color.STEELBLUE)
                     ?.transition()
                     ?.delay(Duration.seconds(1.0))
                     ?.fill(Color.ORANGE)
                     ?.transition()
                     ?.delay(Duration.seconds(1.0))
                     ?.fill(Color.LIGHTGRAY)
                     ?.transition()
                     ?.delay(Duration.millis(n))
                     ?.on(Animation.Status.RUNNING) { println("2nd repeat"); repeat(this) }
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(CyclingTransitionIIDemo::class.java, *args)
        }
    }
}