package kubed.scale


class PointScale<D> : Scale<D, Double> {
    val bandScale: BandScale<D> = BandScale()

    override val domain: List<D>
        get() = bandScale.domain

    override val range: List<Double>
        get() = bandScale.range

    var unknown: Double
        get() = bandScale.unknown
        set(value) {
            bandScale.unknown = value
        }

    var round: Boolean
        get() = bandScale.round
        set(value) {
            bandScale.round = value
        }

    var padding: Double
        get() = bandScale.paddingOuter
        set(value) {
            bandScale.paddingOuter = value
        }

    var align: Double
        get() = bandScale.align
        set(value) {
            bandScale.align = align
        }

    override operator fun invoke(d: D): Double = bandScale.invoke(d)

    override fun ticks(count: Int): List<D> = emptyList()

    fun domain(d: List<D>): PointScale<D> {
        bandScale.domain(d)
        return this
    }

    fun range(r: List<Double>): PointScale<D> {
        bandScale.range(r)
        return this
    }

    fun rangeRound(r: List<Double>): PointScale<D> {
        bandScale.rangeRound(r)
        return this
    }

    fun round(value: Boolean): PointScale<D> {
        bandScale.round = value
        return this
    }

    fun unknown(unk: Double): PointScale<D> {
        bandScale.unknown = unk
        return this
    }

    fun padding(value: Double): PointScale<D> {
        bandScale.paddingOuter = value
        return this
    }
}