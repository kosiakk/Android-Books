package com.kosenkov.androidbooks.collections

/**
 * This implementation of List interface loads its data lazily
 * by requesting external data in fix-sized chunks.
 *
 * This implementation is better suited for GUIs than returning Future<E>, because
 * read operations are constant-time and eager to fail immediately, if the data is not available.
 *
 * Implementations should asynchronously call `setPageData` or `resetPageData` from `enqueueFetch`
 */
abstract class LazyPagedList<E>(protected val pageSize: Int) : AbstractList<E?>() {
    init {
        require(pageSize > 0)
    }

    private val pages = ArrayList<List<E>?>()

    /**
     * Default implementation provides upper estimate of the number of elements
     */
    override val size: Int
        get() = pages.size * pageSize

    private val Page_NotRequested: List<E>? = null          // default value of each element in the array
    private val Page_RequestSent: List<E>? = emptyList()    // request is being processed or has returned no data

    constructor(firstPage: List<E>) : this(firstPage.size) {
        pages.add(firstPage)
    }

    /**
     * Returns data at given linear index across all pages.
     *
     * @returns null if data is not yet available
     */
    @Synchronized
    override fun get(index: Int): E? {
        if (index < 0) throw IndexOutOfBoundsException("index: $index, total size: $size")

        val pageIndex = index / pageSize
        val indexOnPage = index - pageIndex * pageSize // = index.rem(pageSize), but faster

        // page will be null if pageIndex is out of bounds
        val page = pages.getOrNull(pageIndex)
        when (page) {
            Page_NotRequested -> {
                // we don't have this data yet
                pages.setSafe(pageIndex, Page_RequestSent)
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

    /**
     * Implementations should schedule background delivery of data and call `setPageData` callback,
     * when requested data becomes available.
     *
     * This method is called from the GUI thread, therefore must be non-blocking itself.
     */
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
        pages.setSafe(pageIndex, elements)
    }

    /**
     * If request has failed, page request can be discarded, so that it might be repeated
     */
    protected fun resetPageData(pageIndex: Int) {
        pages[pageIndex] = Page_NotRequested
    }


    infix private fun Int.divideRoundUp(divisor: Int) = (this + divisor - 1) / divisor
}

/**
 * Sets value directly to the desired index, adding extra null elements in between if needed.
 */
private fun <E> MutableList<E?>.setSafe(index: Int, value: E) {
    if (index in indices) {
        this[index] = value
    } else {
        require(index >= 0)
        for (missingElement in 1..index - size) {
            // fill possible empty pages between current end of the list and desired index
            add(null)
        }
        add(value)
    }
}
