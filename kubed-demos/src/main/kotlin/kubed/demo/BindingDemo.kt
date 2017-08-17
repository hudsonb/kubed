package kubed.demo

import javafx.application.Application
import javafx.application.Application.launch
import javafx.scene.Group
import javafx.scene.Scene
import javafx.stage.Stage
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

        root.selectAll<Int>("Circle")
            .data((-1..1).toList())
            .enter()
            .append { _, _, _ -> c(Unit) }
            .translateX { d, _, _ -> d * 50.0 }

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

