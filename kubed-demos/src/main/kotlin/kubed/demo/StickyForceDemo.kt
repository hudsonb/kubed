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

class StickyForceDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val width = 960.0
        val height = 500.0

        val root = Group()
        //root.translateX = width / 2
        //root.translateY = height / 2

        val nodes = nodes()
        val links = links(nodes)
        val sim = forceSimulation(nodes) {
            addForce("link", forceLink {
                links(links)
                distance = { _, _, _ -> 40.0 }
            })

           addForce("collide", forceCollision(12.0))

            addForce("charge", forceManyBody { strength = constant(-400.0) })
            addForce("center", forceCenter(width / 2, height / 2))
        }

        sim.start()

        val line = lineSegment<Link> {
            startX = { d, _ -> d.source.x }
            startY = { d, _ -> d.source.y }
            endX = { d, _ -> d.target.x }
            endY = { d, _ -> d.target.y }
        }

        val circle = circle<ForceNode> {
            centerX = { d, _ -> d.x }
            centerY = { d, _ -> d.y }

            radius(12.0)

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

                    this as Circle
                    fill = Color.RED
                }
                .on(MouseEvent.MOUSE_RELEASED) {
                    sim.alphaTarget = 0.0
                }
                .on(MouseEvent.MOUSE_CLICKED) { e ->
                    if(e.clickCount == 2) {
                        val forceNode = datum as ForceNode
                        if (forceNode.fx.isNotNaN() || forceNode.fy.isNotNaN()) {
                            forceNode.fx = Double.NaN
                            forceNode.fy = Double.NaN

                            this as Circle
                            fill = Color.BLACK

                            sim.alpha = 0.3
                            sim.restart()
                        }
                    }
                }


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

    private fun nodes() = listOf(ForceNode(469.0, 410.0),
                                 ForceNode(493.0, 364.0),
                                 ForceNode(442.0, 365.0),
                                 ForceNode(467.0, 314.0),
                                 ForceNode(477.0, 248.0),
                                 ForceNode(425.0, 207.0),
                                 ForceNode(402.0, 155.0),
                                 ForceNode(369.0, 196.0),
                                 ForceNode(350.0, 148.0),
                                 ForceNode(539.0, 222.0),
                                 ForceNode(594.0, 235.0),
                                 ForceNode(582.0, 185.0),
                                 ForceNode(633.0, 200.0))

    private fun links(nodes: List<ForceNode>): List<Link> {
        return listOf(Link(nodes[0], nodes[1]),
                      Link(nodes[1], nodes[2]),
                      Link(nodes[2], nodes[0]),
                      Link(nodes[1], nodes[3]),
                      Link(nodes[3], nodes[2]),
                      Link(nodes[3], nodes[4]),
                      Link(nodes[4], nodes[5]),
                      Link(nodes[5], nodes[6]),
                      Link(nodes[5], nodes[7]),
                      Link(nodes[6], nodes[7]),
                      Link(nodes[6], nodes[8]),
                      Link(nodes[7], nodes[8]),
                      Link(nodes[9], nodes[4]),
                      Link(nodes[9], nodes[11]),
                      Link(nodes[9], nodes[10]),
                      Link(nodes[10], nodes[11]),
                      Link(nodes[11], nodes[12]),
                      Link(nodes[12], nodes[10]))
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(StickyForceDemo::class.java, *args)
        }
    }
}

