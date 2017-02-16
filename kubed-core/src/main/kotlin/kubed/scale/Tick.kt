package kubed.scale

interface Ticker<out D> {
    fun ticks(count: Int = 10): List<D>
}