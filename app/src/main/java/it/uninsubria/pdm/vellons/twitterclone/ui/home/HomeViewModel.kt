package it.uninsubria.pdm.vellons.twitterclone.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import it.uninsubria.pdm.vellons.twitterclone.R
import it.uninsubria.pdm.vellons.twitterclone.tweet.Tweet
import java.text.SimpleDateFormat

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    // Interesting article: https://lgvalle.medium.com/firebase-viewmodels-livedata-cb64c5ee4f95
    // Interesting article: https://medium.com/@deepak140596/firebase-firestore-using-view-models-and-livedata-f9a012233917

    private val TAG = "HomeViewModel"
    private var auth: FirebaseAuth = Firebase.auth
    private var firestore: FirebaseFirestore = Firebase.firestore

    private var tweets: MutableLiveData<List<Tweet>> = MutableLiveData()

    @SuppressLint("SimpleDateFormat")
    fun getTweets(): LiveData<List<Tweet>> {
        if (tweets.value == null) {
            firestore.collection("tweets").whereEqualTo("visible", true)
                .orderBy("postedAt", Query.Direction.DESCENDING)
                .limit(300)
                .get()
                .addOnSuccessListener { documents ->
                    val listOfTweets: MutableList<Tweet> = mutableListOf()
                    for (document in documents) {
                        val tweetPostTimestamp =
                            (document?.data?.get("postedAt") as Timestamp).seconds * 1000
                        val tweetPostDate = (document.data["postedAt"] as Timestamp).toDate()

                        var displayDate =
                            SimpleDateFormat("dd/MM/yyyy - HH:mm").format(tweetPostDate)
                        if (DateUtils.isToday(tweetPostTimestamp)) {
                            displayDate =
                                getString(R.string.today) + SimpleDateFormat(" - HH:mm")
                                    .format(tweetPostDate)
                        }

                        val item = Tweet(
                            id = document.id,
                            userId = document.data["uid"].toString(),
                            user = null, // TweetAdapter will add user info obj
                            displayDate = displayDate,
                            text = document.data["tweet"].toString(),
                            source = document.data["sourcePath"].toString(),
                            photoLink = document.data["photo"].toString(),
                            commentCount = 0,
                            retweetCount = 0,
                            likeCount = (document.data["likes"] as List<*>).size,
                            hasUserLike = (document.data["likes"] as List<*>).contains(auth.currentUser?.uid)
                        )
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

    private fun getString(string: Int): String {
        // https://stackoverflow.com/questions/47628646/how-should-i-get-resourcesr-string-in-viewmodel-in-android-mvvm-and-databindi
        return getApplication<Application>().resources.getString(string)
    }
}