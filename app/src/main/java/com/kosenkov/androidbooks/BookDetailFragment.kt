package com.kosenkov.androidbooks

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kosenkov.androidbooks.books.GoogleBooks
import com.kosenkov.androidbooks.books.GoogleBooksHttp
import kotlinx.android.synthetic.main.activity_book_detail.*
import kotlinx.android.synthetic.main.book_detail.*

/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a [BookListActivity]
 * in two-pane mode (on tablets) or a [BookDetailActivity]
 * on handsets.
 *
 *
 */
class BookDetailFragment : Fragment() {

    private lateinit var mItem: GoogleBooks.VolumeDetails
    private val booksApi = GoogleBooksHttp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!arguments.containsKey(ARG_ITEM_ID)) {
            throw IllegalArgumentException("Book details were asked, but no $ARG_ITEM_ID is given")
        }

        // todo Load the dummy content specified by the fragment
        // arguments. In a real-world scenario, use a Loader
        // to load content from a content provider.
        val volumeId = arguments.getString(ARG_ITEM_ID)!!

        mItem = booksApi.details(volumeId)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.book_detail, container, false)


        detail_toolbar.title = mItem.volume.title
        book_detail.text = mItem.volume.subtitle

        // toDo other details

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
