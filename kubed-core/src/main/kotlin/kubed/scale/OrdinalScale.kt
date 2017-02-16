package kubed.scale

import java.util.*

open class OrdinalScale<D, R> : Scale<D, R> {
    private val index: MutableMap<D, Int> = HashMap()
    override val domain: MutableList<D> = ArrayList()
    override val range: MutableList<R> = ArrayList()
    var unknown: R? = null

    override operator fun invoke(d: D): R {
        if(!index.containsKey(d)) {
            domain.add(d)
            index.put(d, domain.size - 1)
        }

        val i = index[d] ?: return unknown ?: throw IllegalStateException()
        return when {
            range.isEmpty() -> unknown ?: throw IllegalStateException()
            else -> range[i % range.size]
        }
    }

    override fun ticks(count: Int): List<D> = listOf()

    fun domain(d: List<D>) {
        domain.clear()
        index.clear()

        d.forEach {
            if(!index.containsKey(it)) {
                domain.add(it)
                index.put(it, domain.size - 1)
            }
        }
    }

    fun range(r: List<R>) {
        range.clear()
        range.addAll(r)
    }

    fun unknown(value: R) {
        unknown = value
    }
}