package com.kosenkov.androidbooks


abstract class LazyPagedList<E>(totalSize: Int, protected val pageSize: Int) : AbstractList<E?>() {
    init {
        require(totalSize > 0)
        require(pageSize > 0)
    }

    private val pages = arrayOfNulls<List<E>>(totalSize divideRoundUp pageSize)

    override val size = totalSize

    constructor(totalSize: Int, firstPage: List<E>) : this(totalSize, firstPage.size) {
        pages[0] = firstPage
    }

    @Synchronized
    override fun get(index: Int): E? {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("index: $index, lazy size: $size")

        val pageIndex = index / pageSize
        val indexOnPage = index.rem(pageSize)

        if (pageIndex in pages.indices) {
            val page = pages[pageIndex]

            if (page != null && indexOnPage in page.indices) {
                return page[indexOnPage]
            }
        }

        // we don't have this data yet
        enqueueFetch(pageIndex)

        return null
    }

    abstract protected fun enqueueFetch(pageIndex: Int)

    /**
     * Implementations should call this method, when data becomes available
     */
    @Synchronized
    protected fun addAll(pageIndex: Int, elements: List<E>) {

        require(elements.size == pageSize || size - pageIndex * pageSize == elements.size) {
            "List of ${elements.size} elements is added to the page $pageIndex"
        }


        pages[pageIndex] = elements // It would be better to copy the (possibly mutable) list
    }


    infix private fun Int.divideRoundUp(divisor: Int) = (this + divisor - 1) / divisor
}
