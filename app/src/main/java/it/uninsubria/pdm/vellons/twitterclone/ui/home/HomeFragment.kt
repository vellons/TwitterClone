package it.uninsubria.pdm.vellons.twitterclone.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import it.uninsubria.pdm.vellons.twitterclone.R
import it.uninsubria.pdm.vellons.twitterclone.tweet.Tweet
import it.uninsubria.pdm.vellons.twitterclone.tweet.TweetAdapter


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view_home_tweet)
        val mTweetAdapter = TweetAdapter(ArrayList<Tweet>(), context) // Initialize empty
        recyclerView.adapter = mTweetAdapter
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration( // Gray border between tweets
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        recyclerView.setHasFixedSize(true)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        homeViewModel.getTweets()
            .observe(viewLifecycleOwner, Observer { tweetList -> // Observe changes and notify
                if (tweetList != null) {
                    mTweetAdapter.tweetList = tweetList // Update adapter list
                    mTweetAdapter.notifyDataSetChanged()
                }
            })


        // Search filter action
        val editTextSearch: EditText = root.findViewById(R.id.editTextSearch)
        val imageButtonSearchIcon: ImageButton = root.findViewById(R.id.imageButtonSearchIcon)

        imageButtonSearchIcon.setOnClickListener {
            // When search click: hide keyboard and filter tweets
            editTextSearch.clearFocus()
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editTextSearch.windowToken, 0)
            homeViewModel.searchInTweets(editTextSearch.text.toString())
                .observe(viewLifecycleOwner, Observer { tweetList -> // Observe changes and notify
                    if (tweetList != null) {
                        mTweetAdapter.tweetList = tweetList // Update adapter list
                        mTweetAdapter.notifyDataSetChanged()
                    }
                })
        }


        // Refresh action
        val swipeRefresh: SwipeRefreshLayout = root.findViewById(R.id.swipeRefresh)
        swipeRefresh.setColorSchemeColors(resources.getColor(R.color.primary))
        swipeRefresh.setOnRefreshListener {
            homeViewModel.reloadTweets()
                .observe(viewLifecycleOwner, Observer { tweetList -> // Observe changes and notify
                    if (tweetList != null) {
                        mTweetAdapter.tweetList = tweetList // Update adapter list
                        mTweetAdapter.notifyDataSetChanged()
                        swipeRefresh.isRefreshing = false // Stop the loading spinner
                        editTextSearch.setText("")
                        editTextSearch.clearFocus() // Remove focus and close keyboard
                        val imm =
                            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(editTextSearch.windowToken, 0)
                    }
                })
        }

        return root
    }
}