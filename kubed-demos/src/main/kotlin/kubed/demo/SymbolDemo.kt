package kubed.demo

import javafx.application.Application
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.scale.scaleOrdinal
import kubed.scale.schemeCategory10
import kubed.selection.selectAll
import kubed.shape.symbol.SymbolType
import kubed.shape.symbol.symbol
import kubed.shape.symbol.symbols
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

class SymbolDemo: Application() {
    override fun start(primaryStage: Stage) {
        val margin = 40.0

        val root = Group()
        root.translateX = margin
        root.translateY = margin

        val width = 800.0
        val height = 400.0

        val color = scaleOrdinal<Int, Color> {
            range(schemeCategory10())
            domain(symbols().indices.toList())
        }
        val c = symbol<SymbolType> {
            size(250.0)
            type { d, _ -> d }
            translateX { _, i -> i * 40.0 + 25.0 }
            translateYProperty(primaryStage.heightProperty().divide(2).subtract(margin + 25.0 / 2))
            stroke(Color.BLACK)
            fill { _, i -> color(i) }
        }

        root.selectAll<SymbolType>()
                .data(symbols())
                .enter().append { d, i, _ -> c(d, i) }
                .bind({ _, _, _ -> translateYProperty() }, primaryStage!!.heightProperty().divide(2).subtract(margin + 25.0 / 2))

        Thread(Runnable {
            Thread.sleep(1000)

            Platform.runLater { saveAsPng(root) }
        }).start()

        val scene = Scene(root)
        primaryStage.width = width + margin * 2
        primaryStage.height = height + margin * 2

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
            launch(SymbolDemo::class.java, *args)
        }
    }
}

