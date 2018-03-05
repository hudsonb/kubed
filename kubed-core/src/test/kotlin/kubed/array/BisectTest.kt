package kubed.array

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Comparator

internal class BisectTest {

    @Test
    fun bisectLeft() {
        val list1 = listOf(0.0, 2.0, 3.0, 7.0)

        assertEquals(0, bisectLeft(list1, -.2, naturalOrder()))
        assertEquals(0, bisectLeft(list1, 0.0, naturalOrder()))
        assertEquals(1, bisectLeft(list1, 0.3, naturalOrder()))
        assertEquals(1, bisectLeft(list1, 0.9, naturalOrder()))
        assertEquals(1, bisectLeft(list1, 2.0, naturalOrder()))
        assertEquals(2, bisectLeft(list1, 2.2, naturalOrder()))
        assertEquals(2, bisectLeft(list1, 2.7, naturalOrder()))
        assertEquals(3, bisectLeft(list1, 3.1, naturalOrder()))
        assertEquals(3, bisectLeft(list1, 6.7, naturalOrder()))
        assertEquals(3, bisectLeft(list1, 7.0, naturalOrder()))
        assertEquals(4, bisectLeft(list1, 8.0, naturalOrder()))

        val list2 = listOf(3.0, 3.0, 3.0, 3.0, 3.0, 5.0, 5.0)

        assertEquals(0, bisectLeft(list2, 1.0, naturalOrder()))
        assertEquals(0, bisectLeft(list2, 3.0, naturalOrder()))
        assertEquals(5, bisectLeft(list2, 3.1, naturalOrder()))
        assertEquals(5, bisectLeft(list2, 4.9, naturalOrder()))
        assertEquals(5, bisectLeft(list2, 5.0, naturalOrder()))
        assertEquals(7, bisectLeft(list2, 5.2, naturalOrder()))
    }

    @Test
    fun bisectRight() {
        val list1 = listOf(0.0, 2.0, 3.0, 7.0)

        assertEquals(0, bisectRight(list1, -.2, naturalOrder()))
        assertEquals(1, bisectRight(list1, 0.0, naturalOrder()))
        assertEquals(1, bisectRight(list1, 0.3, naturalOrder()))
        assertEquals(1, bisectRight(list1, 0.9, naturalOrder()))
        assertEquals(2, bisectRight(list1, 2.0, naturalOrder()))
        assertEquals(2, bisectRight(list1, 2.2, naturalOrder()))
        assertEquals(2, bisectRight(list1, 2.7, naturalOrder()))
        assertEquals(3, bisectRight(list1, 3.1, naturalOrder()))
        assertEquals(3, bisectRight(list1, 6.7, naturalOrder()))
        assertEquals(4, bisectRight(list1, 7.0, naturalOrder()))
        assertEquals(4, bisectRight(list1, 8.0, naturalOrder()))

        val list2 = listOf(3.0, 3.0, 3.0, 3.0, 3.0, 5.0, 5.0)

        assertEquals(0, bisectRight(list2, 1.0, naturalOrder()))
        assertEquals(5, bisectRight(list2, 3.0, naturalOrder()))
        assertEquals(5, bisectRight(list2, 3.1, naturalOrder()))
        assertEquals(5, bisectRight(list2, 4.9, naturalOrder()))
        assertEquals(7, bisectRight(list2, 5.0, naturalOrder()))
        assertEquals(7, bisectRight(list2, 5.2, naturalOrder()))
    }
}