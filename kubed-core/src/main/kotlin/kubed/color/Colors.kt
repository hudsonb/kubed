@file:Suppress("NOTHING_TO_INLINE")

package kubed.color

import javafx.scene.paint.Color

inline fun rgb(r: Int, g: Int, b: Int) = rgba(r, g, b, 1.0)
inline fun rgba(r: Int, g: Int, b: Int, a: Double) = rgba(r / 255.0, g / 255.0, b / 255.0, a)

inline fun rgb(r: Double, g: Double, b: Double) = rgba(r, g, b, 1.0)
inline fun rgba(r: Double, g: Double, b: Double, a: Double) = Color(r, g, b, a)

inline fun hsl(h: Double, s: Int, l: Int) = hsla(h, s, l, 1.0)
inline fun hsla(h: Double, s: Int, l: Int, a: Double) = hsla(h, s / 100.0, l / 100.0, a)

inline fun hsl(h: Double, s: Double, l: Double) = hsla(h, s, l, 1.0)
inline fun hsla(h: Double, s: Double, l: Double, a: Double) = Hsl(h, s, l, a).toColor()