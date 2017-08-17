package kubed.demo

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.util.Duration
import kubed.ease.*
import kubed.selection.selectAll
import kubed.shape.*
import kubed.transition.*
import java.util.*

class SelectionDemo: Application() {
    val alphabet = "abcdefghijklmnopqrstuvwxyz".toMutableList()

    fun <T:Comparable<T>> shuffle(items: MutableList<T>): List<T>{
        val rg : Random = Random()
        for (i in 0..items.size - 1) {
            val randomPosition = rg.nextInt(items.size)
            val tmp : T = items[i]
            items[i] = items[randomPosition]
            items[randomPosition] = tmp
        }

        return items
    }

    override fun start(primaryStage: Stage?) {
        val root = Group()
        //root.translateY = 200.0

        update(root)

        val timeline = Timeline(KeyFrame(Duration.seconds(2.5), EventHandler<ActionEvent> { _ -> update(root) }))
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()

        val scene = Scene(root)
        primaryStage?.width = 400.0
        primaryStage?.height = 400.0

        primaryStage?.scene = scene
        primaryStage?.show()
    }

    private fun update(root: Group) {
        val font = Font("Courier New", 32.0)

        val sel = root.selectAll<Char>("*")
                      .data(randomChars(), { d, _, _ -> d })

        // Exit
        sel.exit()
           .fill(Color.RED)
           .transition()
           .duration(Duration.millis(750.0))
           .translateY(120.0)
           .opacity(0.0)
           .remove()

        val text = text<Char> {
            text { d, _ -> d.toString() }
            font(font)
            opacity(0.0)
        }

        // Enter
        sel.enter()
           .append { d, _, _ -> text(d) }
           .fill(Color.GREEN)
           .translateX { _, i, _ -> i * 20.0 }
           .transition()
           .duration(Duration.millis(750.0))
           .interpolator(BounceOutInterpolator())
           .translateY(60.0)
           .opacity(1.0)

        // Update
        sel.opacity(1.0)
           .fill(Color.BLACK)
           .transition()
           .duration(Duration.millis(750.0))
           .translateX { _, i, _ -> i * 20.0 }
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(SelectionDemo::class.java, *args)
        }
    }

    private fun randomChars(): List<Char> {
        val shuffled = shuffle(alphabet)
        return shuffled.subList(0, ((Math.random() * 25) + 1).toInt()).sorted()
    }
}
