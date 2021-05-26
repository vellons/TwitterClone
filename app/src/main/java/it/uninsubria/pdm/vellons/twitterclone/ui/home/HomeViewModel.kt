package it.uninsubria.pdm.vellons.twitterclone.ui.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import it.uninsubria.pdm.vellons.twitterclone.tweet.Tweet
import java.text.SimpleDateFormat

class HomeViewModel : ViewModel() {
    // Interesting article: https://lgvalle.medium.com/firebase-viewmodels-livedata-cb64c5ee4f95
    // Interesting article: https://medium.com/@deepak140596/firebase-firestore-using-view-models-and-livedata-f9a012233917

    private val TAG = "HomeViewModel"
    private var auth: FirebaseAuth = Firebase.auth
    private var firestore: FirebaseFirestore = Firebase.firestore
    private var storage: FirebaseStorage = Firebase.storage

    private var tweets: MutableLiveData<List<Tweet>> = MutableLiveData()

    @SuppressLint("SimpleDateFormat")
    fun getTweets(): LiveData<List<Tweet>> {
        if (tweets.value == null) {
            firestore.collection("tweets").whereEqualTo("visible", true)
                .orderBy("postedAt", Query.Direction.DESCENDING).get()
                .addOnSuccessListener { documents ->
                    val listOfTweets: MutableList<Tweet> = mutableListOf()
                    val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm")
                    var position = 0
                    for (document in documents) {
                        val tweetPostDate = (document?.data?.get("postedAt") as Timestamp).toDate()
                        val item = Tweet(
                            id = document.id,
                            userId = document.data["uid"].toString(),
                            user = null, // TweetAdapter will add user info obj
                            displayDate = sdf.format(tweetPostDate),
                            text = document.data["tweet"].toString(),
                            source = document.data["sourcePath"].toString(),
                            photoLink = document.data["photo"].toString(),
                            commentCount = 0,
                            retweetCount = 0,
                            likeCount = 0,
                            hasUserLike = false
                        )
                        position += 1
                        listOfTweets.add(item)
                    }
                    tweets.postValue(listOfTweets)
                    Log.d(TAG, "Tweets list download completed")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error downloading tweets list: ", exception)
                }
        }
        return tweets
    }
}