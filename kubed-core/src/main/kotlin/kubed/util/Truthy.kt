@file:Suppress("NOTHING_TO_INLINE")

package kubed.util

inline fun Any?.isTruthy() = this != null
inline fun Any?.isFalsy() = !isTruthy()

inline fun Boolean?.isTruthy() = this != null && this
inline fun Boolean?.isFalsy() = this == null || this

inline fun Byte?.isTruthy() = this != null && this != 0.toByte()
inline fun Byte?.isFalsy() = !isTruthy()

inline fun Double?.isTruthy() = this != null && this != 0.0 && this != Double.NaN
inline fun Double?.isFalsy() = !isTruthy()

inline fun Float?.isTruthy() = this != null && this != 0f && this != Float.NaN
inline fun Float?.isFalsy() = !isTruthy()

inline fun Int?.isTruthy() = this != null && this != 0
inline fun Int?.isFalsy() = !isTruthy()

inline fun Long?.isTruthy(): Boolean = this != null && this != 0L
inline fun Long?.isFalsy() = !isTruthy()

inline fun Short?.isTruthy(): Boolean = this != null && this != 0.toShort()
inline fun Short?.isFalsy() = !isTruthy()

inline fun String?.isTruthy(): Boolean = this != null && isNotEmpty()
inline fun String?.isFalsy() = !isTruthy()
