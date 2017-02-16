package kubed.array

import java.util.Comparator

fun <T> bisect(list: List<T>, x: T, comparator: Comparator<T>, lo: Int = 0, hi: Int = list.size): Int {
    return bisectRight(list, x, comparator, lo, hi)
}

fun <T> bisectLeft(list: List<T>, x: T, comparator: Comparator<T>, low: Int = 0, high: Int = list.size): Int {
    var lo = low
    var hi = high
    while(lo < hi) {
        val mid = (lo + hi) / 2
        if(comparator.compare(list[mid], x) < 0)
            lo =  mid + 1
        else
            hi = mid
    }

    return lo
}

fun <T> bisectRight(list: List<T>, x: T, comparator: Comparator<T>, low: Int = 0, high: Int = list.size): Int {
    var lo = low
    var hi = high
    while(lo < hi) {
        val mid = (lo + hi) / 2
        if(comparator.compare(list[mid], x) > 0)
            hi = mid
        else
            lo = mid + 1
    }

    return lo
}