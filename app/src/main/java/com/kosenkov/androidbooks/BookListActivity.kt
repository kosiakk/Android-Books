package com.kosenkov.androidbooks

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.kosenkov.androidbooks.books.GoogleBooks
import com.kosenkov.androidbooks.books.GoogleBooksHttp
import kotlinx.android.synthetic.main.activity_book_list.*
import kotlinx.android.synthetic.main.book_list.*
import kotlinx.android.synthetic.main.book_list_content.view.*

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

    lateinit var glide: RequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        setSupportActionBar(toolbar)
        toolbar.title = "Book Search"

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val adapter = setupRecyclerView(book_list)

        if (findViewById(R.id.book_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true
        }

        glide = Glide.with(this)

        backgroundTask(
                inBackground = { q: String ->
                    GoogleBooksHttp().search(q)
                },
                postExecute = { (startIndex, totalItems, items) ->
                    adapter.mValues = items.toList()
                    adapter.notifyDataSetChanged()
                }
        ).execute("Alice")


    }

    private fun setupRecyclerView(recyclerView: RecyclerView): SimpleItemRecyclerViewAdapter {
        val adapter = SimpleItemRecyclerViewAdapter(
                emptyList()
        )
        recyclerView.adapter = adapter
        return adapter
    }

    inner class SimpleItemRecyclerViewAdapter(var mValues: List<GoogleBooks.Volume>) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.book_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.mItem = mValues[position]

            holder.mView.setOnClickListener { v ->
                if (mTwoPane) {
                    val arguments = Bundle()
                    arguments.putString(BookDetailFragment.ARG_ITEM_ID, holder.mItem!!.id)
                    val fragment = BookDetailFragment()
                    fragment.arguments = arguments
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.book_detail_container, fragment)
                            .commit()
                } else {
                    val context = v.context
                    val intent = Intent(context, BookDetailActivity::class.java)
                    intent.putExtra(BookDetailFragment.ARG_ITEM_ID, holder.mItem!!.id)

                    context.startActivity(intent)
                }
            }
        }

        override fun getItemCount(): Int {
            return mValues.size
        }

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

            private var _mItem: GoogleBooks.Volume? = null
            var mItem: GoogleBooks.Volume?
                set(book) {
                    mView.book_title.text = book?.title
                    mView.book_subtitle.text = book?.subtitle
                    glide.load(book?.thumbnailImageLinks).fitCenter().crossFade().into(mView.book_thumbnail)
                }
                get() = _mItem

        }
    }
}
