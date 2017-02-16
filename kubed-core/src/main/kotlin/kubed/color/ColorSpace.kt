package kubed.color

import javafx.scene.paint.Color

const val DARKER = 0.7
const val BRIGHTER = 1.0 / DARKER

interface ColorSpace<T : ColorSpace<T>> {
    fun rgb(): Rgb
    fun brighter(k: Double = BRIGHTER): T
    fun darker(k: Double = DARKER): T

    fun toColor(): Color = rgb().toColor()
}