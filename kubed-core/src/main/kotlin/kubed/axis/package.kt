package kubed.axis

import javafx.geometry.Side
import kubed.scale.Scale

fun <D, R : Number> axisTop(scale: Scale<D, R>) = Axis(Side.TOP, scale)
fun <D, R : Number> axisTop(scale: Scale<D, R>, init: Axis<D, R>.() -> Unit) = axisTop(scale).apply(init)

fun <D, R : Number> axisRight(scale: Scale<D, R>) = Axis(Side.RIGHT, scale)
fun <D, R : Number> axisRight(scale: Scale<D, R>, init: Axis<D, R>.() -> Unit) = axisRight(scale).apply(init)

fun <D, R : Number> axisBottom(scale: Scale<D, R>) = Axis(Side.BOTTOM, scale)
fun <D, R : Number> axisBottom(scale: Scale<D, R>, init: Axis<D, R>.() -> Unit) = axisBottom(scale).apply(init)

fun <D, R : Number> axisLeft(scale: Scale<D, R>) = Axis(Side.LEFT, scale)
fun <D, R : Number> axisLeft(scale: Scale<D, R>, init: Axis<D, R>.() -> Unit) = axisLeft(scale).apply(init)