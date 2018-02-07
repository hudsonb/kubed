package kubed.color.scheme

import javafx.scene.paint.Color

internal fun colors(s: String): List<Color> {
    val colors = ArrayList<Color>()
    var str = s
    while(str.isNotEmpty()) {
        colors += Color.web(str.take(6))
        str = str.substring(6)
    }

    return colors
}

internal fun colors2(s: String) = List<Color>(s.length / 6) {
    val i = it * 6
    Color.web(s.substring(i, i + 6))
}

internal fun scheme(k: Int, schemes: List<String>): List<Color> {
    val i = k - 3
    require(i in schemes.indices)
    return colors2(schemes[i])
}