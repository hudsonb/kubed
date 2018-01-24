@file:Suppress("NOTHING_TO_INLINE")

package kubed.util

inline fun Any?.isTruthy() = !isFalsy()
inline fun Any?.isFalsy() = this == null

inline fun Boolean?.isTruthy() = !isFalsy()
inline fun Boolean?.isFalsy() = this == null || this == false

inline fun Byte?.isTruthy() = !isFalsy()
inline fun Byte?.isFalsy() = this == null || this == 0.toByte()

inline fun Double?.isTruthy() = !isFalsy()
inline fun Double?.isFalsy() = this == null || this == -0.0 || this == 0.0 || isNaN()

inline fun Float?.isTruthy() = !isFalsy()
inline fun Float?.isFalsy() = this == null || this == -0f || this == 0f || isNaN()

inline fun Int?.isTruthy() = !isFalsy()
inline fun Int?.isFalsy() = this == null || this == 0

inline fun Long?.isTruthy() = !isFalsy()
inline fun Long?.isFalsy() = this == null || this == 0L

inline fun Short?.isTruthy() = !isFalsy()
inline fun Short?.isFalsy() = this == null || this == 0.toShort()

inline fun String?.isTruthy() = !isFalsy()
inline fun String?.isFalsy() = this == null || isEmpty()
