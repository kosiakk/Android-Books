package com.kosenkov.androidbooks.view

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.kosenkov.androidbooks.LazyBooksList
import com.kosenkov.androidbooks.R
import com.kosenkov.androidbooks.books.GoogleBooks
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

    private lateinit var searchListAdapter: SimpleItemRecyclerViewAdapter


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

        searchListAdapter = SimpleItemRecyclerViewAdapter()

        book_list.adapter = searchListAdapter

        doSearch("Liza")
    }

    private fun doSearch(query: String) {

        doAsync {
            // blocking operation
            val lazyBooksList = LazyBooksList(query, searchListAdapter)

            uiThread {
                searchListAdapter.setList(lazyBooksList)
            }
        }

    }

    private fun onBookSelected(book: GoogleBooks.Volume) {
        if (mTwoPane) {
            val arguments = Bundle()
            arguments.putString(BookDetailFragment.ARG_ITEM_ID, book.id)
            arguments.putSerializable(BookDetailFragment.ARG_ITEM_DETAILS, book)

            val fragment = BookDetailFragment()
            fragment.arguments = arguments
            supportFragmentManager.beginTransaction()
                    .replace(R.id.book_detail_container, fragment)
                    .commit()
        } else {
            val context = applicationContext
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra(BookDetailFragment.ARG_ITEM_ID, book.id)
            intent.putExtra(BookDetailFragment.ARG_ITEM_DETAILS, book)

            context.startActivity(intent)
        }
    }

    /**
     * This class is fairly simple - it transfers the data from the List<Volume?> to a recycled list row view
     */
    inner class SimpleItemRecyclerViewAdapter : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.BookViewHolder>() {
        private var mValues: List<GoogleBooks.Volume?> = emptyList()
        private val glide = Glide.with(applicationContext)!!

        fun setList(lazyBooksList: List<GoogleBooks.Volume?>) {
            mValues = lazyBooksList
            notifyDataSetChanged()
        }

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            val book = mValues[position]
            holder.setBook(book)

            holder.mView.setOnClickListener(if (book == null)
                notLoadedCallback
            else
                View.OnClickListener {
                    onBookSelected(book)
                }
            )
        }

        private val notLoadedCallback: View.OnClickListener = View.OnClickListener {
            // static no-op function callback, shared for extra bit of performance
        }

        override fun getItemCount() = mValues.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                BookViewHolder(
                        LayoutInflater.from(parent.context)
                                .inflate(R.layout.book_list_content, parent, /* attachToRoot = */false))

        inner class BookViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

            /**
             * Shows available book data
             */
            fun setBook(book: GoogleBooks.Volume?) {
                mView.book_title.text = book?.title ?: "..."
                mView.book_subtitle.text = book?.subtitle ?: ""
                mView.book_thumbnail.loadImage(book?.thumbnailImageLinks)
            }

            private fun ImageView.loadImage(url: String?) {
                Glide.clear(this)

                if (url != null)
                    glide.load(url).fitCenter().crossFade().into(this)
            }

        }

    }
}
