package it.uninsubria.pdm.vellons.twitterclone.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        var tweetList: List<Tweet> = ArrayList<Tweet>()

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view_home_tweet)
        val mTweetAdapter = TweetAdapter(tweetList, context)
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
            .observe(viewLifecycleOwner, Observer { it -> // Observe changes and notify
                tweetList = it
                mTweetAdapter.tweetList = tweetList // Update adapter list
                mTweetAdapter.notifyDataSetChanged()
            })


        // Refresh action
        val swipeRefresh: SwipeRefreshLayout = root.findViewById(R.id.swipeRefresh)
        swipeRefresh.setColorSchemeColors(resources.getColor(R.color.primary))
        swipeRefresh.setOnRefreshListener {
            homeViewModel.getTweets()
                .observe(viewLifecycleOwner, Observer { it -> // Observe changes and notify
                    tweetList = it
                    mTweetAdapter.tweetList = tweetList // Update adapter list
                    mTweetAdapter.notifyDataSetChanged()
                    swipeRefresh.isRefreshing = false // Stop the loading spinner
                })
        }

        return root
    }
}