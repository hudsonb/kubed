package kubed.ease

fun linearInterposlator() = ::LinearInterpolator
fun easeLinear() = ::LinearInterpolator

fun polyInInterpolator(exponent: Double = 3.0) = PolyInInterpolator(exponent)
fun easePolyIn(exponent: Double = 3.0) = PolyInInterpolator(exponent)
fun polyOutInterpolator(exponent: Double = 3.0) = PolyOutInterpolator(exponent)
fun easePolyOut(exponent: Double = 3.0) = PolyOutInterpolator(exponent)
fun polyInOutInterpolator(exponent: Double = 3.0) = PolyInOutInterpolator(exponent)
fun easeInOutInterpolator(exponent: Double = 3.0) = PolyInOutInterpolator(exponent)
