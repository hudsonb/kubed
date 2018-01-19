package kubed.layout.chord

import kubed.math.TAU
import kubed.util.isTruthy
import java.util.*

class ChordDiagram {
    var padAngle = 0.0
    var groupComparator: Comparator<Int>? = null
    var subgroupComparator: Comparator<Double>? = null
    var chordComparator: Comparator<Chord>? = null

    fun chord(matrix: List<List<Double>>): Chords {
        val n = matrix.size
        val groupSums = ArrayList<Double>(n)
        val groupIndex = ArrayList((0 until n).toList())
        val subgroupIndex = ArrayList<List<Int>>()
        val groups = ArrayList<Group?>(n).apply { repeat(n) { add(null) }  }
        val subgroups = Array<Group?>(n * n, { null })
        val chords = Chords(groups)

        var k = 0.0
        var i = -1
        while(++i < n) {
            var x = 0.0
            var j = -1
            while(++j < n) {
                x += matrix[i][j]
            }

            groupSums += x
            subgroupIndex += (0 until n).toList()
            k += x
        }

        // Sort groups
        if(groupComparator != null) {
            groupIndex.sortWith(groupComparator!!)
        }

        // Sort subgroups
        if(subgroupComparator != null) {
           for(j in subgroupIndex.indices) {
               subgroupIndex[j] = subgroupIndex[j].sortedWith(Comparator<Int> { a, b ->
                   subgroupComparator!!.compare(matrix[j][a], matrix[j][b])
               })
           }
        }

        // Convert the sum to scaling factor for [0, 2pi]
        k = Math.max(0.0, TAU - padAngle * n) / k
        val dx = if(k.isTruthy()) padAngle else TAU / n

        // Compute the start and end angle for each group and subgroup
        var x = 0.0
        i = -1
        while(++i < n) {
            val di = groupIndex[i]
            val x0 = x
            var j = -1
            while(++j < n) {
                val dj = subgroupIndex[di][j]
                val v = matrix[di][dj]
                val a0 = x
                x += v * k
                val a1 = x
                subgroups[dj * n + di] = Group(di, dj, a0, a1, v)
            }

            groups[di] = Group(di, -1, x0, x, groupSums[di])
            x += dx
        }

        // Generate chords for each (non-empty) subgroup-subgroup link
        i = -1
        while(++i < n) {
            var j = i - 1
            while(++j < n) {
                val source = subgroups[j * n + i]
                val target = subgroups[i * n + j]

                if(source == null || target == null)
                    continue

                if(source.value.isTruthy() || target.value.isTruthy()) {
                    chords += if(source.value < target.value) Chord(target, source)
                              else Chord(source, target)
                }
            }
        }

        if(chordComparator != null) {
            chords.sortWith(chordComparator!!)
        }

        return chords
    }
}

data class Group(val index: Int, val subindex: Int, val startAngle: Double,
                 val endAngle: Double, val value: Double)

data class Chord(val source: Group, val target: Group)

class Chords(val groups: List<Group?>) : ArrayList<Chord>()