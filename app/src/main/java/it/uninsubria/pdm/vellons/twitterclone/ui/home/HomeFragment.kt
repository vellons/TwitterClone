package it.uninsubria.pdm.vellons.twitterclone.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.pdm.vellons.twitterclone.R
import it.uninsubria.pdm.vellons.twitterclone.tweet.Tweet
import it.uninsubria.pdm.vellons.twitterclone.tweet.TweetAdapter
import it.uninsubria.pdm.vellons.twitterclone.user.User

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val tweetList = generateSampleList(150)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view_home_tweet)
        recyclerView.adapter = TweetAdapter(tweetList, context)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        recyclerView.setHasFixedSize(true)
        return root
    }

    private fun generateSampleList(size: Int): List<Tweet> {
        val list = ArrayList<Tweet>()
        for (i in 0 until size) {
            val user = User(
                id = "$i",
                name = "Name $i",
                username = "user$i",
                userVerified = i % 5 == 0,
            )
            val item = Tweet(
                position = i,
                id = i.toString(),
                user = user,
                displayDate = (i + 1).toString() + " hour ago",
                text = "Tweet $i. Ciao!",
                source = if (i % 4 == 0) "https://example.com/$i" else "",
                commentCount = size - i,
                retweetCount = 20 + i,
                likeCount = if (i % 3 == 0) i + 3 else 0,
                hasUserLike = i % 6 == 0
            )
            list += item
        }
        return list
    }
}