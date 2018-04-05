package kubed.util

fun Boolean.toInt() = if(this) 1 else 0

infix fun Boolean.shl(bits: Int) = this.toInt() shl bits