package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.stage.Stage
import kubed.selection.selectAll

class MatrixHeatmapDemo: Application() {
    override fun start(primaryStage: Stage) {
        val cellSize = 25.0

        val root = Group()

        val data = getData()

        root.selectAll<Data>()
            .data(data)
            .enter()
            .append { _, _, _ -> Group() }
            .translateY { _, i, _ -> i * cellSize }
            .selectAll<Pair<String, Int>>("Group")

        val scene = Scene(root)
        //primaryStage.width = width + margin * 2
        //primaryStage.height = height + margin * 2

        primaryStage.scene = scene
        primaryStage.show()
    }

    fun getData(): List<Data> {
        return listOf(Data(2009, mapOf(Pair("Pedestrian", 147), Pair("Cyclist", 12), Pair("Motor Vehicle", 64))),
                      Data(2010, mapOf(Pair("Pedestrian", 136), Pair("Cyclist", 19), Pair("Motor Vehicle", 65))),
                      Data(2011, mapOf(Pair("Pedestrian", 129), Pair("Cyclist", 20), Pair("Motor Vehicle", 62))),
                      Data(2012, mapOf(Pair("Pedestrian", 140), Pair("Cyclist", 18), Pair("Motor Vehicle", 78))),
                      Data(2013, mapOf(Pair("Pedestrian", 167), Pair("Cyclist", 12), Pair("Motor Vehicle", 76))),
                      Data(2014, mapOf(Pair("Pedestrian", 124), Pair("Cyclist", 19), Pair("Motor Vehicle", 63))),
                      Data(2015, mapOf(Pair("Pedestrian", 126), Pair("Cyclist", 15), Pair("Motor Vehicle", 64))),
                      Data(2016, mapOf(Pair("Pedestrian", 66), Pair("Cyclist", 14), Pair("Motor Vehicle", 30))))
    }

    data class Data(val year: Int, val fatalities: Map<String, Int>)

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(MatrixHeatmapDemo::class.java, *args)
        }
    }
}

