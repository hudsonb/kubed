package kubed.demo

import javafx.application.Application
import javafx.geometry.Insets
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.axis.axisBottom
import kubed.axis.axisLeft
import kubed.interpolate.interpolateRound
import kubed.scale.LinearScale
import kubed.scale.scaleBand
import kubed.scale.scaleLinear
import kubed.selection.selectAll
import kubed.shape.rect

data class LetterFrequency(val letter: Char, val frequency: Double)

class BarChartDemo: Application() {
    override fun start(primaryStage: Stage?) {
        val margin = Insets(20.0, 10.0, 20.0, 10.0)

        val outerWidth = 960.0
        val outerHeight = 500.0
        val innerWidth = outerWidth - margin.left - margin.right
        val innerHeight = outerHeight - margin.top - margin.bottom

        val root = Group()
        root.prefWidth(outerWidth)
        root.prefHeight(outerHeight)
        root.translateX = margin.left + 30.0
        root.translateY = margin.top

        val data = letterFrequencies()

        val x = scaleBand<Char> {
            rangeRound(listOf(0.0, innerWidth))
            domain(data.map { it.letter })
            padding(0.1)
        }

        val y = scaleLinear(::interpolateRound) {
            range(innerHeight, 0.0)
            domain(0.0, data.map { it.frequency }.max() ?: 0.0)
        }

        val xAxis = axisBottom(x)
        xAxis(root.selectAll<Nothing>(".xAxis")
                  .append { _, _, _ -> Group() }
                  .classed("axis", "xAxis")
                  .translateY(innerHeight))

        val yAxis = axisLeft(y) {
            formatter = { d -> (d * 100).toInt().toString() + "%" }
        }
        yAxis(root.selectAll<Nothing>(".yAxis")
                  .append { _, _, _ -> Group() }
                  .classed("axis", "yAxis"))


    val bar = rect<LetterFrequency> {
            translateX { (letter), _ -> x(letter) }
            translateY { d, _ -> y(d.frequency) }
            width(x.bandwidth)
            height { d, _ -> innerHeight - y(d.frequency) }
            fill(Color.STEELBLUE)
            styleClasses("bar")
    }

    root.selectAll<LetterFrequency>(".bar")
        .data(data)
        .enter()
        .append { d, _, _ -> bar(d) }

        val scene = Scene(root)
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(BarChartDemo::class.java, *args)
        }
    }
}

fun letterFrequencies() = listOf(LetterFrequency('A', .08167),
        LetterFrequency('B', .01492),
        LetterFrequency('C', .02782),
        LetterFrequency('D', .04253),
        LetterFrequency('E', .12702),
        LetterFrequency('F', .02288),
        LetterFrequency('G', .02015),
        LetterFrequency('H', .06094),
        LetterFrequency('I', .06966),
        LetterFrequency('J', .00153),
        LetterFrequency('K', .00772),
        LetterFrequency('L', .04025),
        LetterFrequency('M', .02406),
        LetterFrequency('N', .06749),
        LetterFrequency('O', .07507),
        LetterFrequency('P', .01929),
        LetterFrequency('Q', .00095),
        LetterFrequency('R', .05987),
        LetterFrequency('S', .06327),
        LetterFrequency('T', .09056),
        LetterFrequency('U', .02758),
        LetterFrequency('V', .00978),
        LetterFrequency('W', .02360),
        LetterFrequency('X', .00150),
        LetterFrequency('Y', .01974),
        LetterFrequency('Z', .00074))