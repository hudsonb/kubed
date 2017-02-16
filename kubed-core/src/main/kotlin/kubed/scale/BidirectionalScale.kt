package kubed.scale

interface BidirectionalScale<D, R> : Scale<D, R> {
    fun invert(r: R): D?
}