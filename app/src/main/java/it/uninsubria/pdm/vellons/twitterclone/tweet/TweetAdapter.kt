package it.uninsubria.pdm.vellons.twitterclone.tweet

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import it.uninsubria.pdm.vellons.twitterclone.R
import it.uninsubria.pdm.vellons.twitterclone.UserDetailActivity
import it.uninsubria.pdm.vellons.twitterclone.user.User


class TweetAdapter(initialTweetList: List<Tweet>, private val context: Context?) :
    RecyclerView.Adapter<TweetAdapter.TweetViewHolder>() {

    private val TAG = "TweetAdapter"
    var tweetList: List<Tweet> = ArrayList<Tweet>()

    private var auth: FirebaseAuth
    private var firestore: FirebaseFirestore
    private var storage: FirebaseStorage

    init {
        tweetList = initialTweetList
        auth = Firebase.auth
        firestore = Firebase.firestore
        storage = Firebase.storage
    }

    class TweetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Useful: https://www.youtube.com/watch?v=6Gm3eMG8KqI
        // Elements in Tweet.xml
        val name: TextView = itemView.findViewById(R.id.textViewName)
        val username: TextView = itemView.findViewById(R.id.textViewUsername)
        val verifiedBadge: ImageView = itemView.findViewById(R.id.imageViewVerifiedBadge)
        val date: TextView = itemView.findViewById(R.id.textViewTime)
        val text: TextView = itemView.findViewById(R.id.text)
        val sourceLbl: TextView = itemView.findViewById(R.id.textViewSource)
        val source: TextView = itemView.findViewById(R.id.textViewSourceLink)
        val commentCount: TextView = itemView.findViewById(R.id.textViewCommentCount)
        val retweetCount: TextView = itemView.findViewById(R.id.textViewRetweetCount)
        val likeCount: TextView = itemView.findViewById(R.id.textViewLikeCount)
        val imageViewPost: ImageView = itemView.findViewById(R.id.imageViewPost)
        val commentImage: ImageButton = itemView.findViewById(R.id.imageButtonComment)
        val retweetImage: ImageButton = itemView.findViewById(R.id.imageButtonRetweet)
        val likeImage: ImageButton = itemView.findViewById(R.id.imageButtonLike)
        val userImage: ShapeableImageView = itemView.findViewById(R.id.imageViewUserImage)
    }

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        // Binding xml with tweet object from list by position
        val currentItem = tweetList[position]
        if (currentItem.user == null) {  // Check if user is present. If not download from DB
            // Get user photo if present
            val uid = auth.currentUser?.uid
            val userRef = firestore.collection("users").document(uid!!)
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    currentItem.user = User(
                        id = uid,
                        name = document.getString("name") as String,
                        username = document.getString("username") as String,
                        userVerified = document.getBoolean("verified") == true,
                        bio = document.getString("bio"),
                        profilePhoto = document.getString("photo"),
                    )
                    updateUserInfo(holder, position)
                } else {
                    Log.e(TAG, "No such document. User do not exists in DB")
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Failed to get profile info to display tweet", exception)
            }
        }

        // Fill all TextView
        updateUserInfo(holder, position)
        holder.date.text = currentItem.displayDate
        holder.text.text = currentItem.text
        holder.source.text = currentItem.source
        if (currentItem.source != "" && currentItem.source != "null") { // Tweet source
            holder.source.visibility = View.VISIBLE
            holder.sourceLbl.visibility = View.VISIBLE
            holder.source.setOnClickListener {
                var url = holder.source.text
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "https://$url"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url as String?))
                context?.startActivity(browserIntent)
            }
        } else {
            holder.source.visibility = View.GONE
            holder.sourceLbl.visibility = View.GONE
        }
        holder.commentCount.text = currentItem.commentCount.toString()
        holder.retweetCount.text = currentItem.retweetCount.toString()
        holder.likeCount.text = currentItem.likeCount.toString()
        if (currentItem.hasUserLike) {
            holder.likeImage.setImageResource(R.drawable.ic_like_full_24)
            holder.likeImage.setColorFilter(R.color.like_color)
        } else {
            holder.likeImage.setImageResource(R.drawable.ic_like_outline_24)
            holder.likeImage.clearColorFilter()
        }

        if (currentItem.photoLink != "" && currentItem.photoLink != "null") { // Tweet photo
            holder.imageViewPost.setImageResource(R.mipmap.ic_launcher_foreground) // Loader
            holder.imageViewPost.visibility = View.VISIBLE
            holder.imageViewPost.visibility = View.VISIBLE
            val profilePath = currentItem.photoLink
            if (profilePath != null) { // Download profile photo if present
                val storageRef = storage.reference
                val profilePhotoRef = storageRef.child(profilePath)

                val ONE_MEGABYTE: Long = 1024 * 1024
                // .getBytes take as a parameter the maximum download size accepted
                profilePhotoRef.getBytes(8 * ONE_MEGABYTE)
                    .addOnSuccessListener { bytes ->
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        holder.imageViewPost.setImageBitmap(bmp)
                    }.addOnFailureListener {
                        Log.e(TAG, "Failed to download tweet post image ", it)
                    }
            }
        } else {
            holder.imageViewPost.visibility = View.GONE
            holder.imageViewPost.visibility = View.GONE
        }

        holder.commentImage.setOnClickListener {
            displayNotYetImplemented()
        }
        holder.retweetImage.setOnClickListener {
            displayNotYetImplemented()
        }

        // Like/Remove like
        holder.likeImage.setOnClickListener {
            val tweetRef = firestore.collection("tweets").document(currentItem.id)
            if (!currentItem.hasUserLike) {
                currentItem.hasUserLike = true
                currentItem.likeCount = currentItem.likeCount + 1
                holder.likeCount.text = ((holder.likeCount.text as String).toInt() + 1).toString()
                holder.likeImage.setImageResource(R.drawable.ic_like_full_24)
                holder.likeImage.setColorFilter(R.color.like_color)

                // Add like
                val tweetUpdate = hashMapOf(
                    "likes" to FieldValue.arrayUnion(currentItem.userId),
                )
                tweetRef.update(tweetUpdate as Map<String, Any>)
                    .addOnSuccessListener {
                        Log.d(TAG, "Like added for tweet ${currentItem.id}")
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to add like for tweet ${currentItem.id}")
                    }
            } else {
                currentItem.hasUserLike = false
                currentItem.likeCount = currentItem.likeCount - 1
                holder.likeCount.text = ((holder.likeCount.text as String).toInt() - 1).toString()
                holder.likeImage.setImageResource(R.drawable.ic_like_outline_24)
                holder.likeImage.clearColorFilter()

                // Remove like
                val tweetUpdate = hashMapOf(
                    "likes" to FieldValue.arrayRemove(currentItem.userId),
                )
                tweetRef.update(tweetUpdate as Map<String, Any>)
                    .addOnSuccessListener {
                        Log.d(TAG, "Like removed for tweet ${currentItem.id}")
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "Failed to remove like for tweet ${currentItem.id}")
                    }
            }
        }

        // User detail activity
        holder.userImage.setOnClickListener {
            openUserDetailsActivity(currentItem.user)
        }
        holder.name.setOnClickListener {
            openUserDetailsActivity(currentItem.user)
        }
        holder.verifiedBadge.setOnClickListener {
            openUserDetailsActivity(currentItem.user)
        }
        holder.username.setOnClickListener {
            openUserDetailsActivity(currentItem.user)
        }
    }

    private fun updateUserInfo(holder: TweetViewHolder, position: Int) {
        val currentItem = tweetList[position]
        holder.name.text = currentItem.user?.name
        holder.username.text = ("@" + currentItem.user?.username)
        holder.username.visibility = View.GONE
        holder.verifiedBadge.visibility =
            if (currentItem.user?.userVerified == true) View.VISIBLE else View.GONE

        val profilePath = currentItem.user?.profilePhoto
        if (profilePath != null) { // Download profile photo if present
            val storageRef = storage.reference
            val profilePhotoRef = storageRef.child(profilePath)

            val ONE_MEGABYTE: Long = 1024 * 1024
            // .getBytes take as a parameter the maximum download size accepted
            profilePhotoRef.getBytes(8 * ONE_MEGABYTE)
                .addOnSuccessListener { bytes ->
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    holder.userImage.setImageBitmap(bmp)
                }.addOnFailureListener {
                    Log.e(TAG, "Failed to download user image ", it)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.tweet, parent, false)
        return TweetViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return tweetList.size
    }

    private fun displayNotYetImplemented() {
        val toast = Toast.makeText(
            context,
            context?.getString(R.string.not_implemented_yet),
            Toast.LENGTH_SHORT
        )
        toast.setGravity(Gravity.TOP, 0, 75)
        toast.show()
    }

    private fun openUserDetailsActivity(user: User?) {
        if (user == null) return
        val intent = Intent(context, UserDetailActivity::class.java)
        intent.putExtra("id", user.id)
        intent.putExtra("name", user.name)
        intent.putExtra("username", user.username)
        intent.putExtra("userVerified", user.userVerified)
        intent.putExtra("bio", user.bio)
        intent.putExtra("profilePhoto", user.profilePhoto)
        context?.startActivity(intent)
    }
}