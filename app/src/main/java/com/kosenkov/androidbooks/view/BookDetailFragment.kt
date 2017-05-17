package com.kosenkov.androidbooks.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kosenkov.androidbooks.R
import com.kosenkov.androidbooks.books.GoogleBooksCache
import com.kosenkov.androidbooks.books.GoogleBooksHttp
import kotlinx.android.synthetic.main.activity_book_detail.view.*
import kotlinx.android.synthetic.main.book_detail.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a [BookListActivity]
 * in two-pane mode (on tablets) or a [BookDetailActivity]
 * on handsets.
 *
 *
 */
class BookDetailFragment : Fragment() {

    private val booksApi = GoogleBooksCache(GoogleBooksHttp())
    private lateinit var volumeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!arguments.containsKey(ARG_ITEM_ID)) {
            throw IllegalArgumentException("Book details were asked, but no $ARG_ITEM_ID is given")
        }

        volumeId = arguments.getString(ARG_ITEM_ID)!!
    }

    override fun onViewCreated(rootView: View?, savedInstanceState: Bundle?) {
        doAsync {
            val mItem = booksApi.details(volumeId)

            uiThread {
                activity.title = mItem.volume.title

                val theToolbar = rootView!!.detail_toolbar
                if (theToolbar != null) {
                    theToolbar.title = mItem.volume.title
                } else {
//                    rootView
                }
                rootView.book_detail.text = mItem.volume.subtitle

                // toDo other details
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater!!.inflate(R.layout.book_detail, container, false)

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        val ARG_ITEM_ID = "volume_id"
    }
}
