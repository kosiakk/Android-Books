package com.kosenkov.androidbooks.collections

/**
 * This implementation of List interface loads its data lazily
 * by requesting external data in fixed pages.
 *
 * This implementation is better suited for GUIs than returning Future<E>, because
 * read operations are constant-time and eager to fail immediately, if the data is not available.
 *
 * Implementations should asynchronously call `setPageData` or `resetPageData` from `enqueueFetch
 */
abstract class LazyPagedList<E>(totalSize: Int, protected val pageSize: Int) : AbstractList<E?>() {
    init {
        require(totalSize > 0)
        require(pageSize > 0)
    }

    override val size = totalSize

    private val pages = arrayOfNulls<List<E>>(totalSize divideRoundUp pageSize)

    private val Page_NotRequested: List<E>? = null          // default value of each element in the array
    private val Page_RequestSent: List<E>? = emptyList()    // request is being processed or has returned no data

    constructor(totalSize: Int, firstPage: List<E>) : this(totalSize, firstPage.size) {
        pages[0] = firstPage
    }

    /**
     * Returns data at given linear index across all pages.
     *
     * @returns null if data is not yet available
     */
    @Synchronized
    override fun get(index: Int): E? {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("index: $index, total size: $size")

        val pageIndex = index / pageSize
        val indexOnPage = index - pageIndex * pageSize // = index.rem(pageSize), but faster

        // Array is prepared for all the pages in advance
        // This can be changed to Map<PageIndex, List> if needed
        assert(pageIndex in pages.indices)

        val page = pages[pageIndex]
        when (page) {
            Page_NotRequested -> {
                // we don't have this data yet
                pages[pageIndex] = Page_RequestSent
                enqueueFetch(pageIndex)
            }
            Page_RequestSent -> {
                // request is currently pending
            }
            else -> {
                if (indexOnPage in page!!.indices)
                    return page[indexOnPage]
            }
        }

        // there is no data yet
        return null
    }

    abstract protected fun enqueueFetch(pageIndex: Int)

    /**
     * Implementations should call this method, when data becomes available
     */
    @Synchronized
    protected fun setPageData(pageIndex: Int, elements: List<E>) {

        require(elements.size <= pageSize) {
            // Some pages contain less data, than expected, potentially resulting in gaps

            "List of ${elements.size} elements is added to the page $pageIndex, but the page size is $pageSize"
        }

        // It would be safer to copy the (possibly mutable) list, but the method is `protected` anyways
        pages[pageIndex] = elements
    }

    /**
     * If request has failed, page request can be discarded
     */
    protected fun resetPageData(pageIndex: Int) {
        pages[pageIndex] = Page_NotRequested
    }


    infix private fun Int.divideRoundUp(divisor: Int) = (this + divisor - 1) / divisor
}
