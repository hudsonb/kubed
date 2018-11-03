package kubed.collections

/**
 * Returns a list containing elements at indices in the specified range.
 */
fun <T> List<T>.slice(start: Int, end: Int = size - 1): List<T> = slice(start..end)

