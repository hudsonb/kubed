package kubed.shape.symbol

fun symbols(): List<SymbolType> = listOf(symbolCircle(),
                                         symbolCross(),
                                         symbolDiamond(),
                                         symbolSquare(),
                                         symbolStar(),
                                         symbolTriangle(),
                                         symbolWye())

fun symbolCircle(): SymbolType = Circle()
fun symbolCross(): SymbolType = Cross()
fun symbolDiamond(): SymbolType = Diamond()
fun symbolSquare(): SymbolType = Square()
fun symbolStar(): SymbolType = Star()
fun symbolTriangle(): SymbolType = Triangle()
fun symbolWye(): SymbolType = Wye()
