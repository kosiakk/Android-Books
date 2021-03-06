package com.kosenkov.androidbooks.collections

import org.junit.Assert.*
import org.junit.Test

/**
 * Created by Alexander Kosenkov on 19.04.2017.
 */

class PagedListTest {

    /**
     * Test data becomes available immediately on request
     */
    class Words : LazyPagedList<String>(listOf("hello", "world")) {

        override fun enqueueFetch(pageIndex: Int) {
            print(" (Fetching page $pageIndex...) ")

            val data = listOf(
                    "${pageIndex * 2}: page $pageIndex first",
                    "${pageIndex * 2 + 1}: page $pageIndex second"
            )

            setPageData(pageIndex, data)
        }

        override fun get(index: Int): String? {
            print("get[$index] ")
            val ans = super.get(index)
            println("= $ans")
            return ans
        }
    }

    @Test
    fun testInitialNull() {
        val list: List<String?> = Words()

        assertEquals("hello", list[0])
        assertEquals("world", list[1])

        assertNull(list[2]) // element is not loaded on the first request
        assertEquals("2: page 1 first", list[2])
        assertEquals("3: page 1 second", list[3])

        assertNull(list[4]) // element was not loaded on the first request
        assertEquals("4: page 2 first", list[4])

        assertThrows<IndexOutOfBoundsException> {
            list[-1]
        }

    }

    /**
     * This test case never gets any data
     */
    class NoData : LazyPagedList<String>(20) {
        val requestCounts = IntArray(5) { 0 }

        override fun enqueueFetch(pageIndex: Int) {
            requestCounts[pageIndex]++
        }
    }

    @Test
    fun testNoData() {
        val list = NoData()

        for (i in 21..99) {
            assertNull("no data must be returned", list[i])
        }

        assertArrayEquals("Each page must be requested once, except the first",
                intArrayOf(0, 1, 1, 1, 1), list.requestCounts)
    }

}