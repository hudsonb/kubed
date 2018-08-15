package kubed.util

fun Boolean.toInt() = if(this) 1 else 0

infix fun Boolean.shl(bits: Int) = this.toInt() shl bits

infix fun Boolean.shr(bits: Int) = this.toInt() shr bits

infix fun Boolean.or(bits: Int) = this.toInt() or bits

infix fun Int.or(bit: Boolean) = this or bit.toInt()