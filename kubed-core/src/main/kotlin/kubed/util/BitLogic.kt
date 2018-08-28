package kubed.util

infix fun Boolean.or(bits: Int) = this.toInt() or bits
infix fun Int.or(bit: Boolean) = this or bit.toInt()

infix fun Boolean.and(bits: Int) = this.toInt() and bits
infix fun Int.and(bit: Boolean) = this and bit.toInt()