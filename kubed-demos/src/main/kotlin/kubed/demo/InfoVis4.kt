package kubed.demo

import javafx.application.Application
import javafx.application.Application.launch
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.stage.Stage
import javafx.util.Duration
import kubed.scale.scaleBand
import kubed.scale.scaleLinear
import kubed.selection.selectAll
import kubed.shape.rect
import kubed.transition.transition
import sun.text.normalizer.UTF16.append
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class InfoVis4 : Application() {
    override fun start(primaryStage: Stage?) {
        val width = 150.0
        val height = 50.0

        val root = Group()
        root.prefWidth(width)
        root.prefHeight(height)

        val data = listOf(Point2D(5.0, .8 * height),
                Point2D(12.0, .7 * height),
                Point2D(19.0, .5 * height),
                Point2D(26.0, .4 * height),
                Point2D(33.0, .3 * height),
                Point2D(40.0, .9 * height),
                Point2D(47.0, .6 * height),
                Point2D(54.0, .8 * height),
                Point2D(61.0, .5 * height),
                Point2D(68.0, .4 * height),
                Point2D(75.0, .4 * height),
                Point2D(82.0, .7 * height),
                Point2D(89.0, .5 * height),
                Point2D(89.0, .6 * height),
                Point2D(96.0, .8 * height))

        val bar = rect<Point2D> {
            height { d, _ -> d.y }
            translateX { d, _ -> d.x }
            width(5.0)
            fill(Color.STEELBLUE)
        }

        root.selectAll<Point2D>(".bar")
            .data(data)
            .enter()
            .append { d, _, _ -> bar(d) }
            .translateX { d, _, _ -> d.x }
            .translateY(-height)
            .transition()
            .duration(Duration.seconds(1.0))
            .delay { _, i, _ -> Duration.millis(i * 250.0) }
            .translateY { d, _, _ -> height - d.y }

        Thread(Runnable {
            Thread.sleep(2300)
            Platform.runLater { saveAsPng(root) }
        }).start()


        val scene = Scene(root)
        primaryStage?.scene = scene
        primaryStage?.show()
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
            launch(InfoVis4::class.java, *args)
        }
    }
}