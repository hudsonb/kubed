package kubed.shape

import javafx.geometry.VPos
import javafx.scene.text.Font
import javafx.scene.text.FontSmoothingType
import javafx.scene.text.TextAlignment
import javafx.scene.text.TextBoundsType

enum class TextAnchor {
    START,
    MIDDLE,
    END
}

class Text<T> : Shape<Text<T>, T>() {
    var x: (T, Int) -> Double = { _, _ -> 0.0 }
    var y: (T, Int) -> Double = { _, _ -> 0.0 }
    var text: (T, Int) -> String = { _, _ -> throw IllegalStateException("text must be specified") }
    var font: ((T, Int) -> Font)? = null
    var textOrigin: ((T, Int) -> VPos)? = null
    var boundsType: ((T, Int) -> TextBoundsType)? = null
    var strikethrough: ((T, Int) -> Boolean)? = null
    var textAlignment: ((T, Int) -> TextAlignment)? = null
    var fontSmoothingType: ((T, Int) -> FontSmoothingType)? = null
    var wrappingWidth: ((T, Int) -> Double)? = null
    var underline: ((T, Int) -> Boolean)? = null
    var lineSpacing: ((T, Int) -> Double)? = null
    var textAnchor: ((T, Int) -> TextAnchor)? = null

    fun x(value: Double) = x { _, _ -> value }
    fun x(value: (T, Int) -> Double): Text<T> {
        x = value
        return this
    }

    fun y(value: Double) = y { _, _ -> value }
    fun y(value: (T, Int) -> Double): Text<T> {
        y = value
        return this
    }

    fun text(value: String) = text { _, _ -> value }
    fun text(value: (T, Int) -> String): Text<T> {
        text = value
        return this
    }

    fun font(value: Font) = font { _, _ -> value }
    fun font(value: ((T, Int) -> Font)?): Text<T> {
        font = value
        return this
    }

    fun textOrigin(value: VPos) = textOrigin { _, _ -> value }
    fun textOrigin(value: (T, Int) -> VPos): Text<T> {
        textOrigin = value
        return this
    }

    fun textAlignment(value: TextAlignment) = textAlignment { _, _ -> value }
    fun textAlignment(value: ((T, Int) -> TextAlignment)?): Text<T> {
        textAlignment = value
        return this
    }

    fun boundsType(value: TextBoundsType) = boundsType { _, _ -> value }
    fun boundsType(value: ((T, Int) -> TextBoundsType)?): Text<T> {
        boundsType = value
        return this
    }

    fun fontSmoothingType(value: FontSmoothingType) = fontSmoothingType { _, _ -> value }
    fun fontSmoothingType(value: ((T, Int) -> FontSmoothingType)?): Text<T> {
        fontSmoothingType = value
        return this
    }

    fun wrappingWidth(value: Double) = wrappingWidth { _, _ -> value }
    fun wrappingWidth(value: ((T, Int) -> Double)?): Text<T> {
        wrappingWidth = value
        return this
    }

    fun lineSpacing(value: Double) = lineSpacing { _, _ -> value }
    fun lineSpacing(value: ((T, Int) -> Double)?): Text<T> {
        lineSpacing = value
        return this
    }

    fun strikethrough(value: Boolean) = strikethrough { _, _ -> value }
    fun strikethrough(value: ((T, Int) -> Boolean)?): Text<T> {
        underline = value
        return this
    }

    fun underline(value: Boolean) = underline { _, _ -> value }
    fun underline(value: ((T, Int) -> Boolean)?): Text<T> {
        underline = value
        return this
    }

    fun textAnchor(value: TextAnchor) = textAnchor { _, _ -> value }
    fun textAnchor(value: ((T, Int) -> TextAnchor)?): Text<T> {
        textAnchor = value
        return this
    }

    override operator fun invoke(d: T, i: Int): javafx.scene.shape.Shape {
        val text = javafx.scene.text.Text()
        text.x = x(d, i)
        text.y = y(d, i)
        text.text = text(d, i)
        text.font = font?.invoke(d, i) ?: text.font
        text.boundsType = boundsType?.invoke(d, i) ?: text.boundsType
        text.isStrikethrough = strikethrough?.invoke(d, i) ?: text.isStrikethrough
        text.textAlignment = textAlignment?.invoke(d, i) ?: text.textAlignment
        text.fontSmoothingType = fontSmoothingType?.invoke(d, i) ?: text.fontSmoothingType
        text.wrappingWidth = wrappingWidth?.invoke(d, i) ?: text.wrappingWidth
        text.isUnderline = underline?.invoke(d, i) ?: text.isUnderline
        text.textOrigin = textOrigin?.invoke(d, i) ?: text.textOrigin
        text.lineSpacing = lineSpacing?.invoke(d, i) ?: text.lineSpacing

        val anchor = textAnchor?.invoke(d, i) ?: TextAnchor.START
        when(anchor) {
           TextAnchor.MIDDLE -> {
               text.x = text.x - text.layoutBounds.width / 2

               var modifying = false
               text.layoutBoundsProperty().addListener { _, _, newBounds ->
                   if(!modifying) {
                       modifying = true
                       text.x = text.x + newBounds.width / 2
                       modifying = false
                   }
               }
           }
           TextAnchor.END -> {
               text.x = text.x - text.layoutBounds.width

               var modifying = false
               text.layoutBoundsProperty().addListener { _, _, newBounds ->
                   if(!modifying) {
                       modifying = true
                       text.x = text.x - newBounds.width
                       modifying = false
                   }
               }
           }
        }

        apply(d, i, text)

        return text
    }
}