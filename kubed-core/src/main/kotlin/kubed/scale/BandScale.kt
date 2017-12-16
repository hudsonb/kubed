package kubed.scale

import java.util.*

class BandScale<D> : Scale<D, Double> {
    private val index: MutableMap<D, Int> = HashMap()
    override val domain: MutableList<D> = ArrayList()
    override val range: MutableList<Double> = ArrayList(2)
    var unknown: Double = Double.NaN

    var round: Boolean = false
        set(value) {
            field = value
            rescale()
        }

    var paddingInner: Double = 0.0
        set(value) {
            field = value
            rescale()
        }

    var paddingOuter: Double = 0.0
        set(value) {
            field = value
            rescale()
        }

    var align: Double = 0.5
        set(value) {
            field = value
            rescale()
        }

    var bandwidth: Double = 0.0
        private set

    private var ordinalRange: MutableList<Double> = ArrayList()

    init {
        range.add(0.0)
        range.add(1.0)
    }

    override operator fun invoke(d: D): Double {
        if(!index.containsKey(d)) {
            domain.add(d)
            index.put(d, domain.size - 1)
        }

        val i: Int = index[d] ?: return unknown
        return if(ordinalRange.isEmpty()) unknown else ordinalRange[i]
    }

    override fun ticks(count: Int): List<D> = domain

    fun domain(d: List<D>): BandScale<D> {
        domain.clear()
        index.clear()

        d.forEach {
            if(!index.containsKey(it)) {
                domain.add(it)
                index.put(it, domain.size - 1)
            }
        }

        rescale()

        return this
    }

    fun range(r: List<Double>): BandScale<D> {
        range.clear()
        range.addAll(r)
        rescale()

        return this
    }

    fun rangeRound(vararg r: Double): BandScale<D> = rangeRound(r.toList())

    fun rangeRound(r: List<Double>): BandScale<D> {
        round = true
        range(r)
        return this
    }

    fun round(value: Boolean): BandScale<D> {
        round = value
        rescale()
        return this
    }

    fun unknown(unk: Double): BandScale<D> {
        unknown = unk
        return this
    }

    fun paddingInner(value: Double): BandScale<D> {
        paddingInner = value
        return this
    }

    fun paddingOuter(value: Double): BandScale<D> {
        paddingOuter = value
        return this
    }

    fun padding(value: Double): BandScale<D> {
        paddingInner = value
        paddingOuter = value
        return this
    }

    private fun rescale() {
        val n = domain.size
        if(range.isEmpty())
            return

        val reverse = range.last() < range.first()
        var start = if(reverse) range.last() else range.first()
        val stop = if(reverse) range.first() else range.last()
        var step = (stop - start) / Math.max(1.0, n - paddingInner + paddingOuter * 2)
        if(round)
            step = Math.floor(step)

        start += (stop - start - step * (n - paddingInner)) * align
        bandwidth = step * (1 - paddingInner)
        if(round) {
            start = Math.round(start).toDouble()
            bandwidth = Math.round(bandwidth).toDouble()
        }

        val values: Array<Double> = Array(n, { start + step * it })
        if(reverse) values.reverse()
        ordinalRange.clear()
        ordinalRange.addAll(values)
    }
}