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
    var x: (T) -> Double = { 0.0 }
    var y: (T) -> Double = { 0.0 }
    var text: (T) -> String = { throw IllegalStateException("text must be specified") }
    var font: ((T) -> Font)? = null
    var textOrigin: ((T) -> VPos)? = null
    var boundsType: ((T) -> TextBoundsType)? = null
    var strikethrough: ((T) -> Boolean)? = null
    var textAlignment: ((T) -> TextAlignment)? = null
    var fontSmoothingType: ((T) -> FontSmoothingType)? = null
    var wrappingWidth: ((T) -> Double)? = null
    var underline: ((T) -> Boolean)? = null
    var lineSpacing: ((T) -> Double)? = null
    var textAnchor: ((T) -> TextAnchor)? = null

    fun x(value: Double) = x { value }
    fun x(value: (T) -> Double): Text<T> {
        x = value
        return this
    }

    fun y(value: Double) = y { value }
    fun y(value: (T) -> Double): Text<T> {
        y = value
        return this
    }

    fun text(value: String) = text { value }
    fun text(value: (T) -> String): Text<T> {
        text = value
        return this
    }

    fun font(value: Font) = font { value }
    fun font(value: ((T) -> Font)?): Text<T> {
        font = value
        return this
    }

    fun textOrigin(value: VPos) = textOrigin { value }
    fun textOrigin(value: (T) -> VPos): Text<T> {
        textOrigin = value
        return this
    }

    fun textAlignment(value: TextAlignment) = textAlignment { value }
    fun textAlignment(value: ((T) -> TextAlignment)?): Text<T> {
        textAlignment = value
        return this
    }

    fun boundsType(value: TextBoundsType) = boundsType { value }
    fun boundsType(value: ((T) -> TextBoundsType)?): Text<T> {
        boundsType = value
        return this
    }

    fun fontSmoothingType(value: FontSmoothingType) = fontSmoothingType { value }
    fun fontSmoothingType(value: ((T) -> FontSmoothingType)?): Text<T> {
        fontSmoothingType = value
        return this
    }

    fun wrappingWidth(value: Double) = wrappingWidth { value }
    fun wrappingWidth(value: ((T) -> Double)?): Text<T> {
        wrappingWidth = value
        return this
    }

    fun lineSpacing(value: Double) = lineSpacing { value }
    fun lineSpacing(value: ((T) -> Double)?): Text<T> {
        lineSpacing = value
        return this
    }

    fun strikethrough(value: Boolean) = strikethrough { value }
    fun strikethrough(value: ((T) -> Boolean)?): Text<T> {
        underline = value
        return this
    }

    fun underline(value: Boolean) = underline { value }
    fun underline(value: ((T) -> Boolean)?): Text<T> {
        underline = value
        return this
    }

    fun textAnchor(value: TextAnchor) = textAnchor { value }
    fun textAnchor(value: ((T) -> TextAnchor)?): Text<T> {
        textAnchor = value
        return this
    }

    override operator fun invoke(d: T): javafx.scene.shape.Shape {
        val text = javafx.scene.text.Text()
        text.x = x(d)
        text.y = y(d)
        text.text = text(d)
        text.font = font?.invoke(d) ?: text.font
        text.boundsType = boundsType?.invoke(d) ?: text.boundsType
        text.isStrikethrough = strikethrough?.invoke(d) ?: text.isStrikethrough
        text.textAlignment = textAlignment?.invoke(d) ?: text.textAlignment
        text.fontSmoothingType = fontSmoothingType?.invoke(d) ?: text.fontSmoothingType
        text.wrappingWidth = wrappingWidth?.invoke(d) ?: text.wrappingWidth
        text.isUnderline = underline?.invoke(d) ?: text.isUnderline
        text.textOrigin = textOrigin?.invoke(d) ?: text.textOrigin
        text.lineSpacing = lineSpacing?.invoke(d) ?: text.lineSpacing

        val anchor = textAnchor?.invoke(d) ?: TextAnchor.START
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

        apply(d, text)

        return text
    }
}