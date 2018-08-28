package kubed.demo

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.stage.Stage
import kubed.force.*
import kubed.selection.datum
import kubed.selection.selectAll
import kubed.shape.circle
import kubed.shape.lineSegment
import kubed.util.isNotNaN

class ForceDirectedLatticeDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val width = 960.0
        val height = 960.0

        val root = Group()
        root.translateX = width / 2
        root.translateY = height / 2

        val n = 20

        val nodes = (0 until n * n).map { ForceNode() }
        val links = ArrayList<Link>()
        for(y in 0 until n) {
            for(x in 0 until n) {
                if(y > 0) links += Link(nodes[(y - 1) * n + x], nodes[y * n + x])
                if(x > 0) links += Link(nodes[y * n + (x - 1)], nodes[y * n + x])
            }
        }

        val sim = forceSimulation(nodes) {
            addForce("charge", forceManyBody { strength = constant(-30.0) })

            addForce("link", forceLink {
                links(links)
                strength = { _, _, _ -> 1.0 }
                distance = { _, _, _ -> 20.0 }
                iterations = 10
            })
        }

        val line = lineSegment<Link> {
            startX = { d, _ -> d.source.x }
            startY = { d, _ -> d.source.y }
            endX = { d, _ -> d.target.x }
            endY = { d, _ -> d.target.y }
        }

        val circle = circle<ForceNode> {
            centerX = { d, _ -> d.x }
            centerY = { d, _ -> d.y }

            radius(3.0)

            styleClasses(".node")
        }

        val selLinks = root.selectAll<Link>(".link")
                .data(links)
                .enter()
                .append { d, _, _ -> line(d) }

        val selNodes = root.selectAll<ForceNode>(".node")
                .data(nodes)
                .enter()
                .append { d, _, _ -> circle(d) }
                .on(MouseEvent.MOUSE_PRESSED) {
                    sim.alphaTarget = 0.3
                    sim.restart()
                }
                .on(MouseEvent.MOUSE_DRAGGED) { e ->
                    val forceNode = datum as ForceNode
                    forceNode.fx = e.x
                    forceNode.fy = e.y
                }
                .on(MouseEvent.MOUSE_RELEASED) {
                    val forceNode = datum as ForceNode
                    forceNode.fx = Double.NaN
                    forceNode.fy = Double.NaN
                    sim.alphaTarget = 0.3
                    sim.restart()
                }

        sim.start()
        sim.addListener(object : SimulationListener {
            override fun tick() {
                selNodes.data(nodes)
                        .forEach<Circle> { d, _, _ ->
                            centerX = d.x
                            centerY = d.y
                        }

                selLinks.data(links)
                        .forEach<Line> { d, _, _ ->
                            startX = d.source.x
                            startY = d.source.y
                            endX = d.target.x
                            endY = d.target.y
                        }
            }

            override fun end() {
            }
        })

        val scene = Scene(root)
        primaryStage?.width = width
        primaryStage?.height = height

        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(ForceDirectedLatticeDemo::class.java, *args)
        }
    }
}

