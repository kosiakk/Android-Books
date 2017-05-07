package com.kosenkov.androidbooks

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.kosenkov.androidbooks.books.GoogleBooks
import com.kosenkov.androidbooks.books.GoogleBooksHttp
import kotlinx.android.synthetic.main.activity_book_list.*
import kotlinx.android.synthetic.main.book_list.*
import kotlinx.android.synthetic.main.book_list_content.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * An activity represents a list of Books.
 *
 * It allows searching and quickly presents very long lists of results.
 * Besides usual use of ListViewAdapter it performs asynchronous lazy loading of results.
 *
 * This activity has different presentations for handset and tablet-size devices.
 * On handsets, the activity presents a list of items, which when touched,
 * lead to a [BookDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class BookListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var mTwoPane: Boolean = false // unfortunately late init is not available for primitive types

    private lateinit var glide: RequestManager
    private lateinit var searchListAdapter: SimpleItemRecyclerViewAdapter

    private val booksApi = GoogleBooksHttp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        setSupportActionBar(toolbar)
        toolbar.title = "Book Search"

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        if (findViewById(R.id.book_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true
        }

        glide = Glide.with(this)
        searchListAdapter = SimpleItemRecyclerViewAdapter()

        book_list.adapter = searchListAdapter

        doSearch("Liza")
    }

    private fun doSearch(query: String) {

        doAsync {
            val firstPage = booksApi.search(query)  // blocking operation

            uiThread {
                searchListAdapter.mValues = LazyBooksList(query, firstPage)
                searchListAdapter.notifyDataSetChanged()
            }

        }

    }

    inner class LazyBooksList(val searchQuery: String, firstPage: GoogleBooks.Volumes)
        : LazyPagedList<GoogleBooks.Volume>(firstPage.totalItems, firstPage.items.toList()) {

        override fun enqueueFetch(pageIndex: Int) {
            Log.v("LazyList", "enqueueFetch(pageIndex=$pageIndex)")
            doAsync {
                val startIndex = pageIndex * pageSize

                // blocking call
                val result = booksApi.search(searchQuery, startIndex)

                setPageData(pageIndex, result.items.toList())

                uiThread {
                    // Only the original thread that created a view hierarchy can touch its views.
                    searchListAdapter.notifyItemRangeChanged(startIndex, pageSize)
                }
            }
        }
    }

    inner class SimpleItemRecyclerViewAdapter : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.BookViewHolder>() {
        var mValues: List<GoogleBooks.Volume?> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                BookViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.book_list_content, parent, /* attachToRoot = */false))

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            val book = mValues[position]
            holder.setBook(book)

            holder.mView.setOnClickListener(if (book == null)
                notLoadedCallback
            else
                loadedCallback(book)
            )
        }

        private val notLoadedCallback: View.OnClickListener = View.OnClickListener {
            // static no-op function callback, shared for extra bit of performance
        }

        private fun loadedCallback(book: GoogleBooks.Volume) = View.OnClickListener { view ->
            if (mTwoPane) {
                val arguments = Bundle()
                arguments.putString(BookDetailFragment.ARG_ITEM_ID, book.id)
                val fragment = BookDetailFragment()
                fragment.arguments = arguments
                supportFragmentManager.beginTransaction()
                        .replace(R.id.book_detail_container, fragment)
                        .commit()
            } else {
                val context = view.context
                val intent = Intent(context, BookDetailActivity::class.java)
                intent.putExtra(BookDetailFragment.ARG_ITEM_ID, book.id)

                context.startActivity(intent)
            }
        }

        override fun getItemCount() = mValues.size

        inner class BookViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

            fun setBook(book: GoogleBooks.Volume?) {

                mView.book_title.text = book?.title ?: "..."
                mView.book_subtitle.text = book?.subtitle ?: ""

                mView.book_thumbnail.imageUrlLazy = book?.thumbnailImageLinks
            }

            private var ImageView.imageUrlLazy: String?
                get() = null
                set(url) {
                    Glide.clear(this)
                    if (url != null)
                        glide.load(url).fitCenter().crossFade().into(this)
                }

        }
    }

}
