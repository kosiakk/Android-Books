package com.kosenkov.androidbooks.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kosenkov.androidbooks.R
import com.kosenkov.androidbooks.books.GoogleBooks
import com.kosenkov.androidbooks.booksApi
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

    private lateinit var volumeId: String
    private var volumeData: GoogleBooks.Volume? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!arguments.containsKey(ARG_ITEM_ID)) {
            throw IllegalArgumentException("Book details were asked, but no $ARG_ITEM_ID is given")
        }

        volumeId = arguments.getString(ARG_ITEM_ID)!!
        volumeData = arguments.getSerializable(ARG_ITEM_DETAILS) as GoogleBooks.Volume?
    }

    override fun onViewCreated(rootView: View?, savedInstanceState: Bundle?) {
        val cachedPreview = volumeData
        if (cachedPreview != null) {
            // This activity
            applyVolume(cachedPreview, rootView)
        }

        doAsync {
            val mItem = booksApi.details(volumeId)
            val volume = mItem.volume
            uiThread {
                applyVolume(volume, rootView)
            }
        }
    }

    private fun applyVolume(volume: GoogleBooks.Volume, rootView: View?) {
        activity.title = volume.title

        val theToolbar = rootView!!.detail_toolbar
        if (theToolbar != null) {
            theToolbar.title = volume.title
        } else {
            //                    rootView
        }
        rootView.book_detail.text = volume.subtitle

        // toDo other details
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
        val ARG_ITEM_DETAILS = "volume"
    }
}
