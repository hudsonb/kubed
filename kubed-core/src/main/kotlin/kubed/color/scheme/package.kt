package kubed.color.scheme

import javafx.scene.paint.Color

internal fun colors(s: String) = s.chunkedSequence(6)
                                  .map { Color.web(it) }
                                  .toList()

internal fun scheme(k: Int, schemes: List<String>): List<Color> {
    val i = k - 3
    require(i in schemes.indices)
    return colors(schemes[i])
}