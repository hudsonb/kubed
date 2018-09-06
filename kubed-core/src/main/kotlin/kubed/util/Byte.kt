package kubed.util

infix fun Byte.shl(other: Byte) = this.toInt() shl other.toInt()

infix fun Byte.and(other: Int) = this.toInt() and other