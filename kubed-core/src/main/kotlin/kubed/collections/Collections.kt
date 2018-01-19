package kubed.collections

/**
 * Returns a list containing elements at indices in the specified range.
 */
public fun <T> List<T>.slice(start: Int, end: Int = size): List<T> = subList(start, end).toList()

