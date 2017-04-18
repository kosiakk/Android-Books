package com.kosenkov.androidbooks

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.kosenkov.androidbooks.books.GoogleBooks
import com.kosenkov.androidbooks.books.GoogleBooksHttp
import kotlinx.android.synthetic.main.book_list.*

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        toolbar.title = title

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val adapter = setupRecyclerView(book_list)

        backgroundTask(
                inBackground = { q: String ->
                    GoogleBooksHttp().syncSearch(q)
                },
                postExecute = { (startIndex, totalItems, items) ->
                    adapter.mValues = items.toList()
                    adapter.notifyDataSetChanged()
                }
        ).execute("Alice")

        if (findViewById(R.id.book_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true
        }


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
            holder.mIdView.text = mValues[position].id
            holder.mContentView.text = mValues[position].title

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
            val mIdView: TextView
            val mContentView: TextView
            var mItem: GoogleBooks.Volume? = null

            init {
                mIdView = mView.findViewById(R.id.id) as TextView
                mContentView = mView.findViewById(R.id.content) as TextView
            }

            override fun toString(): String {
                return super.toString() + " '" + mContentView.text + "'"
            }
        }
    }
}
