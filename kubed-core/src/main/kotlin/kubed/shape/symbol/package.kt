package kubed.shape.symbol

fun <T> symbol(): Symbol<T> = Symbol()
fun <T> symbol(init: Symbol<T>.() -> Unit): Symbol<T> {
    val symbol = Symbol<T>()
    symbol.init()
    return symbol
}

fun symbols(): List<SymbolType> = listOf(symbolCircle(),
                                         symbolCross(),
                                         symbolDiamond(),
                                         symbolSquare(),
                                         symbolStar(),
                                         symbolTriangle(),
                                         symbolWye(),
                                         symbolHexagon(),
                                         symbolPentagon(),
                                         symbolX())

fun symbolCircle(): SymbolType = Circle()
fun symbolCross(): SymbolType = Cross()
fun symbolDiamond(): SymbolType = Diamond()
fun symbolSquare(): SymbolType = Square()
fun symbolStar(): SymbolType = Star()
fun symbolTriangle(): SymbolType = Triangle()
fun symbolWye(): SymbolType = Wye()

fun symbolHexagon(): SymbolType = Hexagon()
fun symbolPentagon(): SymbolType = Pentagon()
fun symbolX(): SymbolType = X()
