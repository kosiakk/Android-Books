package com.kosenkov.androidbooks

/**
 * Created by Alexander Kosenkov on 19.04.2017.
 */

class PagedListTest {

    class Worlds : LazyPagedList<String>(5, listOf("hello", "world")) {

        override fun enqueueFetch(pageIndex: Int) {
            print(" (Fetching page $pageIndex...) ")

            val data = listOf(
                    "${pageIndex * 2}: page $pageIndex first",
                    "${pageIndex * 2 + 1}: page $pageIndex second"
            )

            addAll(pageIndex, data)
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

        val list = Worlds()
        assertEquals("hello", list[0])
        assertEquals("world", list[1])

        assertNull(list[2]) // element was not loaded
        assertEquals("2: page 1 first", list[2])
        assertEquals("3: page 1 second", list[3])

        assertNull(list[4]) // element was not loaded
        assertEquals("4: page 2 first", list[4])

        try {
            val outside = list[5]
            fail("IndexOutOfBoundsException should have been thrown")
        } catch (err: IndexOutOfBoundsException) {
        }

    }


}