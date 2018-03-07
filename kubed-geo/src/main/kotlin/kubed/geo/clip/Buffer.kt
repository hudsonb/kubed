package kubed.geo.clip

import kubed.geo.GeometryStream
import java.util.*

class Buffer : GeometryStream {
    private val lines = LinkedList<List<DoubleArray>>()
    private lateinit var line: LinkedList<DoubleArray>

    override fun point(x: Double, y: Double, z: Double) {
        line.add(doubleArrayOf(x, y))
    }

    override fun lineStart() {
        line = LinkedList()
        lines.add(line)
    }

    fun rejoin() {
        if(lines.size > 1) {
            val l = LinkedList<DoubleArray>(lines.removeLast())
            l.addAll(lines.removeFirst())
            lines.add(l)
        }
    }

    fun result(): List<List<DoubleArray>> {
        val r = ArrayList(lines)
        lines.clear()
        return r
    }
}