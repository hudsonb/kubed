package kubed.demo

import javafx.animation.FadeTransition
import javafx.animation.FillTransition
import javafx.animation.ParallelTransition
import javafx.application.Application
import javafx.application.Application.launch
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.Stage
import javafx.util.Duration
import kubed.selection.selectAll
import kubed.shape.circle

class BindingDemo: Application() {
    override fun start(stage: Stage) {
        val root = Group()

        val width = 800.0
        val height = 400.0
        val r = 25.0

        val c = circle<Unit> {
            layoutXProperty(stage.widthProperty().divide(2.0))
            layoutYProperty(stage.heightProperty().divide(2.0).subtract(r / 2))
            radius(r)
        }

//        root.selectAll<Int>("Circle")
//            .data((-1..1).toList())
//            .enter()
//            .append { _, _, _ -> c(Unit) }
//            .translateX { d, _, _ -> d * 50.0 }

        val circle = Circle(50.0, 50.0, 25.0)
        root.children += circle

        val fill = FillTransition(Duration.seconds(10.0), circle)
        fill.toValue = Color.RED
        fill.onFinished = EventHandler { println("fill finished") }

        val fade = FadeTransition(Duration.seconds(5.0), circle)
        fade.toValue = 0.5
        fade.onFinished = EventHandler { println("fade finished") }

        val pt = ParallelTransition(fill, fade)
        pt.onFinished = EventHandler { println("pt finished") }
        pt.play()

        val scene = Scene(root)
        stage.width = width
        stage.height = height

        stage.scene = scene
        stage.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(BindingDemo::class.java, *args)
        }
    }
}

