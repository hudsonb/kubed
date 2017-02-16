package kubed.util

inline fun Any?.isTruthy(): Boolean = this != null
inline fun Any?.isFalsy(): Boolean = !isTruthy()

inline fun Boolean?.isTruthy(): Boolean = this != null && this
inline fun Byte?.isTruthy(): Boolean = this != null && this != 0.toByte()
inline fun Double?.isTruthy(): Boolean = this != null && this != 0.0 && this != Double.NaN
inline fun Float?.isTruthy(): Boolean = this != null && this != 0f && this != Float.NaN
inline fun Int?.isTruthy(): Boolean = this != null && this != 0
inline fun Long?.isTruthy(): Boolean = this != null && this != 0L
inline fun Short?.isTruthy(): Boolean = this != null && this != 0.toShort()
inline fun String?.isTruthy(): Boolean = this != null && isNotEmpty()

inline fun <E> Collection<E>?.isTruthy(): Boolean = this != null && isNotEmpty()
inline fun <E> Array<E>?.isTruthy(): Boolean = this != null && isNotEmpty()
