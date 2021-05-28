package it.uninsubria.pdm.vellons.twitterclone

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
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
import it.uninsubria.pdm.vellons.twitterclone.tweet.TweetAdapter
import java.text.SimpleDateFormat

class UserDetailActivity : AppCompatActivity() {
    private val TAG = "UserDetailActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var tweets: MutableLiveData<List<Tweet>> = MutableLiveData()
    private lateinit var mTweetAdapter: TweetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_detail)
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage

        val name: TextView = findViewById(R.id.textViewProfileName)
        val username: TextView = findViewById(R.id.textViewProfileUsername)
        val verifiedBadge: ImageView = findViewById(R.id.imageViewProfileVerifiedBadge)
        val bio: TextView = findViewById(R.id.textViewProfileBio)
        val profileImageImage: ShapeableImageView = findViewById(R.id.imageViewProfileUserImage)

        // Get passed intent extra data
        val profileId = intent.getStringExtra("id")
        val profileName = intent.getStringExtra("name")
        val profileUsername = intent.getStringExtra("username")
        val profileUserVerified = intent.getBooleanExtra("userVerified", false)
        val profileBio = intent.getStringExtra("bio")
        val profilePhoto = intent.getStringExtra("profilePhoto")

        // Show info
        if (profileId != null && profileName != null && profileUsername != null) {
            name.text = profileName
            username.text = ("@$profileUsername")
            verifiedBadge.visibility = if (profileUserVerified) View.VISIBLE else View.GONE
            bio.text = profileBio
        }

        // Show user tweet
        val tweetList: List<Tweet> = ArrayList<Tweet>()
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_user_tweet)
        mTweetAdapter = TweetAdapter(tweetList, this)
        recyclerView.adapter = mTweetAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration( // Gray border between tweets
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        recyclerView.setHasFixedSize(true)

        // Load user's tweet
        if (profileId != null) {
            getUserTweets(profileId)
        }

        // Show profile photo
        if (profileId != null && profilePhoto != null) {
            // Get user photo if present
            val storageRef = storage.reference
            val profilePhotoRef = storageRef.child(profilePhoto)

            val ONE_MEGABYTE: Long = 1024 * 1024
            // .getBytes take as a parameter the maximum download size accepted
            profilePhotoRef.getBytes(8 * ONE_MEGABYTE)
                .addOnSuccessListener { bytes ->
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    profileImageImage.setImageBitmap(bmp)
                }.addOnFailureListener {
                    Log.e(TAG, "profilePhoto")
                }
        }
    }

    fun goBack(v: View) {
        finish()
    }

    @SuppressLint("SimpleDateFormat")
    fun getUserTweets(uid: String): LiveData<List<Tweet>> {
        if (tweets.value == null) {
            firestore.collection("tweets").whereEqualTo("visible", true)
                .whereEqualTo("uid", uid)  // Filter by user
                .orderBy("postedAt", Query.Direction.DESCENDING)
                .limit(1000)
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
                    mTweetAdapter.tweetList = listOfTweets // Update adapter list
                    mTweetAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error downloading tweets list: ", exception)
                }
        }
        return tweets
    }
}