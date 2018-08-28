package kubed.util

infix fun Boolean.shl(bits: Int) = this.toInt() shl bits
infix fun Int.shl(bit: Boolean) = this shl bit.toInt()

infix fun Boolean.shr(bits: Int) = this.toInt() shr bits
infix fun Int.shr(bit: Boolean) = this shr bit.toInt()