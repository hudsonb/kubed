package kubed.shape

import kubed.shape.symbol.Symbol

fun <T> arc() = Arc<T>()
fun <T> arc(init: Arc<T>.() -> Unit): Arc<T> {
    val arc = Arc<T>()
    arc.init()
    return arc
}

fun <T> area() = Area<T>()
fun <T> area(init: Area<T>.() -> Unit): Area<T> {
    val area = Area<T>()
    area.init()
    return area
}

fun <T> circle() = Circle<T>()
fun <T> circle(init: Circle<T>.() -> Unit): Circle<T> {
    val circle = Circle<T>()
    circle.init()
    return circle
}

fun <T> cubicCurve() = CubicCurve<T>()
fun <T> cubicCurve(init: CubicCurve<T>.() -> Unit): CubicCurve<T> {
    val curve = CubicCurve<T>()
    curve.init()
    return curve
}

fun <T> ellipse() = Ellipse<T>()
fun <T> ellipse(init: Ellipse<T>.() -> Unit): Ellipse<T> {
    val ellipse = Ellipse<T>()
    ellipse.init()
    return ellipse
}

fun <T> line() = Line<T>()
fun <T> line(init: Line<T>.() -> Unit): Line<T> {
    val line = Line<T>()
    line.init()
    return line
}

fun <T> lineSegment() = LineSegment<T>()
fun <T> lineSegment(init: LineSegment<T>.() -> Unit): LineSegment<T> {
    val line = LineSegment<T>()
    line.init()
    return line
}

fun <T> polygon() = Polygon<T>()
fun <T> polygon(init: Polygon<T>.() -> Unit): Polygon<T> {
    val poly = Polygon<T>()
    poly.init()
    return poly
}

fun <T> polyline() = Polyline<T>()
fun <T> polyline(init: Polyline<T>.() -> Unit): Polyline<T> {
    val line = Polyline<T>()
    line.init()
    return line
}

fun <T> quadCurve() = QuadCurve<T>()
fun <T> quadCurve(init: QuadCurve<T>.() -> Unit): QuadCurve<T> {
    val curve = QuadCurve<T>()
    curve.init()
    return curve
}

fun <T> radialLine() = RadialLine<T>()
fun <T> radialLine(init: RadialLine<T>.() -> Unit): RadialLine<T> {
    val line = RadialLine<T>()
    line.init()
    return line
}

fun <T> rect() = Rectangle<T>()
fun <T> rect(init: Rectangle<T>.() -> Unit): Rectangle<T> {
    val rect = Rectangle<T>()
    rect.init()
    return rect
}

fun <T> symbol(): Symbol<T> = Symbol()
fun <T> symbol(init: Symbol<T>.() -> Unit): Symbol<T> {
    val symbol = Symbol<T>()
    symbol.init()
    return symbol
}

fun <T> text() = Text<T>()
fun <T> text(init: Text<T>.() -> Unit): Text<T> {
    val text = Text<T>()
    text.init()
    return text
}

fun <T> pie() = Pie<T>()
fun <T> pie(init: Pie<T>.() -> Unit) = Pie<T>().apply { init.invoke(this) }