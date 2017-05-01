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
 * An activity representing a list of Books. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [BookDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class BookListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var mTwoPane: Boolean = false

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

    inner class LazyBooksList(val searchQuery: String, result: GoogleBooks.Volumes)
        : LazyPagedList<GoogleBooks.Volume>(result.totalItems, result.items.toList()) {

        override fun enqueueFetch(pageIndex: Int) {
            Log.i("LazyList", "enqueueFetch(pageIndex=$pageIndex)")
            doAsync {
                val startIndex = pageIndex * pageSize

                val result = booksApi.search(searchQuery, startIndex)

                setPageData(pageIndex, result.items.toList())

                uiThread {
                    // Only the original thread that created a view hierarchy can touch its views.
                    searchListAdapter.notifyItemRangeChanged(startIndex, pageSize)
                }
            }
        }
    }

    inner class SimpleItemRecyclerViewAdapter : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {
        var mValues: List<GoogleBooks.Volume?> = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.book_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setBook(mValues[position])

            holder.mView.setOnClickListener { v ->
                val item = mValues[position] ?: return@setOnClickListener

                if (mTwoPane) {
                    val arguments = Bundle()
                    arguments.putString(BookDetailFragment.ARG_ITEM_ID, item.id)
                    val fragment = BookDetailFragment()
                    fragment.arguments = arguments
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.book_detail_container, fragment)
                            .commit()
                } else {
                    val context = v.context
                    val intent = Intent(context, BookDetailActivity::class.java)
                    intent.putExtra(BookDetailFragment.ARG_ITEM_ID, item.id)

                    context.startActivity(intent)
                }
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView?) {
            super.onDetachedFromRecyclerView(recyclerView)
        }

        override fun getItemCount() = mValues.size

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

            fun setBook(book: GoogleBooks.Volume?) {

                mView.book_title.text = book?.title ?: "..."
                mView.book_subtitle.text = book?.subtitle ?: ""

                mView.book_thumbnail.imageUrlLazy = book?.thumbnailImageLinks
            }

            private var ImageView.imageUrlLazy: String?
                get() = throw UnsupportedOperationException("get ImageView.imageUrlLazy")
                set(url) {
                    Glide.clear(this)
                    glide.load(url).fitCenter().crossFade().into(this)
                }

        }
    }

}
