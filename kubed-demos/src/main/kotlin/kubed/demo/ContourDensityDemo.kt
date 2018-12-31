package kubed.demo

import com.univocity.parsers.common.ParsingContext
import com.univocity.parsers.common.processor.ObjectRowProcessor
import com.univocity.parsers.conversions.Conversions
import com.univocity.parsers.tsv.TsvParser
import com.univocity.parsers.tsv.TsvParserSettings
import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Scene
import javafx.stage.Stage
import kubed.contour.Contour
import kubed.contour.contourDensity
import kubed.geo.path.geoPath
import kubed.geo.projection.identity
import kubed.interpolate.color.interpolateYlGnBu
import kubed.path.PathContext
import kubed.scale.scaleLog
import kubed.scale.scaleSequential
import kubed.selection.selectAll

class ContourDensityDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val margin = Insets(20.0, 30.0, 30.0, 40.0)

        val outerWidth = 960.0
        val outerHeight = 960.0
        val innerWidth = outerWidth - margin.left - margin.right
        val innerHeight = outerHeight - margin.top - margin.bottom

        val x = scaleLog<Double> {
            domain(listOf(2e-1, 5e0))
            range(listOf(margin.left, innerWidth))
        }

        val y = scaleLog<Double> {
            domain(listOf(3e2, 2e4))
            range(listOf(innerHeight, margin.top))
        }

        val color = scaleSequential(interpolateYlGnBu()) {
            domain(listOf(0.0, 1.8))
        }

        val diamonds = readData()

        val root = Group()
        root.selectAll<Contour>("*")
            .data(contourDensity(x = { d: Diamond, _, _ -> x(d.carat) },
                                 y = { d: Diamond, _, _ -> y(d.price) }) {
                width = innerWidth
                height = innerHeight
                bandwidth = 10.0
            }(diamonds))
            .enter()
            .append { d, _, _ -> geoPath(null, PathContext())(d.geometry) }
            .fill { d, _, _ -> color(d.value) }

        val scene = Scene(root)
        primaryStage?.width = outerWidth
        primaryStage?.height = outerHeight

        primaryStage?.scene = scene
        primaryStage?.show()
    }

    private fun readData(): List<Diamond> {
        val data = ArrayList<Diamond>()

        val processor = object : ObjectRowProcessor() {
            override fun rowProcessed(row: Array<out Any>?, context: ParsingContext?) {
                if(row != null) data += Diamond(row[0] as Double, row[1] as Double)
            }
        }
        processor.convertIndexes(Conversions.toDouble()).set(0)
        processor.convertIndexes(Conversions.toDouble()).set(1)

        val settings = TsvParserSettings()
        settings.isLineSeparatorDetectionEnabled = true
        settings.isHeaderExtractionEnabled = true
        settings.setProcessor(processor)

        val parser = TsvParser(settings)
        parser.parse(javaClass.getResourceAsStream("/data/diamonds.tsv"))

        return data
    }
}

data class Diamond(val carat: Double, val price: Double)

fun main(vararg args: String) {
    Application.launch(ContourDensityDemo::class.java, *args)
}