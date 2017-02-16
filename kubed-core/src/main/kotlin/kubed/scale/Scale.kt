package kubed.scale

interface Scale<D, out R> {
    val domain: List<D>
    val range: List<R>

    operator fun invoke(d: D): R

    fun ticks(count: Int = 10): List<D>
}
