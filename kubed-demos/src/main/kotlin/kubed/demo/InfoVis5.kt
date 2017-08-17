package kubed.demo

import javafx.application.Application
import javafx.application.Application.launch
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.VPos
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.scale.scaleLinear
import kubed.selection.selectAll
import kubed.shape.rect
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class InfoVis5: Application() {
    override fun start(primaryStage: Stage) {
        val data = listOf(4.0, 8.0, 14.0, 16.0, 23.0, 28.0)

        val width = 420.0
        val barHeight = 20.0

        val x = scaleLinear<Double> {
            domain(0.0, data.max()!!)
            range(0.0, width)
        }

        val root = Group()

        val rect = rect<Double> {
            width { d, _ -> x(d) }
            height(barHeight - 1)
            translateY { _, i -> i * barHeight}
            fill(Color.STEELBLUE)
        }

        root.selectAll<Double>()
            .data(data)
            .enter()
            .append { d, i, _ -> rect(d, i) }

        saveAsPng(root)

        val scene = Scene(root)
        primaryStage.width = width
        primaryStage.height = width
        primaryStage.scene = scene
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

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(InfoVis5::class.java, *args)
        }
    }
}