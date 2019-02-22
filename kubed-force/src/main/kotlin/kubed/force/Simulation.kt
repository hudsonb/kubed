package kubed.force

import kubed.timer.Timer
import kubed.timer.timer
import java.lang.Math.random
import kotlin.math.*

fun <T> forceSimulation(data: List<T>, initialX: (d: T) -> Double = { _ -> Double.NaN }, initialY: (d: T) -> Double = { _ -> Double.NaN }, init: Simulation.() -> Unit) {
    forceSimulation(data.map { ForceNode(initialX(it), initialY(it)) }, init)
}

fun forceSimulation(n: Int, width: Double, height: Double, init: Simulation.() -> Unit) =
        forceSimulation((0 until n).map { ForceNode(random() * width, random() * height) }, init)

fun forceSimulation(nodes: List<ForceNode>, init: Simulation.() -> Unit) = Simulation().apply {
    this.nodes.clear()
    this.nodes.addAll(nodes)
    initializeNodes()
    init(this)
}

data class ForceNode(var x: Double = Double.NaN, var y: Double = Double.NaN,
                     var fx: Double = Double.NaN, var fy: Double = Double.NaN,
                     var vx: Double = 0.0, var vy: Double = 0.0,
                     var index: Int = -1) {
    val properties = HashMap<String, Any>()

    operator fun get(key: String): Any? {
        return properties[key]
    }

    operator fun set(key: String, value: Any) {
        properties[key] = value
    }
}

interface SimulationListener {
    fun tick()
    fun end()
}

class Simulation {
    private val listeners = ArrayList<SimulationListener>()

    private val forces = HashMap<String, Force>()

    private lateinit var stepper: Timer

    private var running = false

    var nodes = ArrayList<ForceNode>()
        set(value) {
            field = value
            initializeNodes()
            forces.values.forEach { it.initialize(nodes) }
        }

    var initialRadius = 10.0
    var initialAngle = PI * (3 - sqrt(5.0))
    var alpha = 1.0
        set(value) {
            require(value in 0.0..1.0)
            field = value
        }

    var alphaMin = 0.001
        set(value) {
            require(value in 0.0..1.0)
            field = value
        }

    var alphaDecay = 1 - alphaMin.pow(1 / 300.0)
        set(value) {
            require(value in 0.0..1.0)
            field = value
        }

    var alphaTarget = 0.0
        @Synchronized get
        @Synchronized set(value) {
            require(value in 0.0..1.0)
            field = value
        }

    var velocityDecay = 0.4
        get() = 1.0 - field
        set(value) {
            require(value in 0.0..1.0)
            field = 1.0 - value
        }

    init {
        initializeNodes()
    }

    fun addForce(name: String, force: Force) {
        force.initialize(nodes)
        forces[name] = force
    }

    fun removeForce(name: String) {
        forces.remove(name)
    }

    fun start() = restart()

    fun restart() {
        if(running) stepper.stop()

        running = true
        stepper = timer { if(running) step() }
    }

    fun addListener(listener: SimulationListener) = listeners.add(listener)
    fun removeListener(listener: SimulationListener) = listeners.remove(listener)

    internal fun initializeNodes() {
        nodes.forEachIndexed { i, node ->
            node.index = i
            if(node.x.isNaN() || node.y.isNaN()) {
                val radius = initialRadius * sqrt(i.toDouble())
                val angle = i * initialAngle
                node.x = radius * cos(angle)
                node.y = radius * sin(angle)
            }

            if(node.vx.isNaN() || node.vy.isNaN()) {
                node.vx = 0.0
                node.vy = 0.0
            }
        }
    }

    fun tick(): Boolean {
        if(alpha < alphaMin) return false

        alpha += (alphaTarget - alpha) * alphaDecay

        forces.values.forEach { it(alpha) }

        for(node in nodes) {
            if(node.fx.isNaN()) {
                node.vx *= velocityDecay
                node.x += node.vx
            }
            else {
                node.x = node.fx
                node.vx = 0.0
            }
            if(node.fy.isNaN()) {
                node.vy *= velocityDecay
                node.y += node.vy
            }
            else {
                node.y = node.fy
                node.vy = 0.0
            }
        }

        return alpha < alphaMin
    }

    private fun step() {
        tick()

        listeners.forEach { it.tick() }
        if(alpha < alphaMin) {
            running = false
            stepper.stop()
            listeners.forEach { it.end() }
        }
    }
}