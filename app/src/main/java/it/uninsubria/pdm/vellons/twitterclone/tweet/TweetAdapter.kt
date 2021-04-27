package it.uninsubria.pdm.vellons.twitterclone.tweet

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.pdm.vellons.twitterclone.R

class TweetAdapter(private val tweetList: List<Tweet>, private val context: Context?) :
// Useful: https://www.youtube.com/watch?v=6Gm3eMG8KqI
    RecyclerView.Adapter<TweetAdapter.TweetViewHolder>() {

    class TweetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        val commentImage: ImageButton = itemView.findViewById(R.id.imageButtonComment)
        val retweetImage: ImageButton = itemView.findViewById(R.id.imageButtonRetweet)
        val likeImage: ImageButton = itemView.findViewById(R.id.imageButtonLike)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        val currentItem = tweetList[position]
        holder.name.text = currentItem.name
        holder.username.text = "@" + currentItem.username
        holder.verifiedBadge.visibility = if (currentItem.userVerified) View.VISIBLE else View.GONE
        holder.date.text = currentItem.displayDate
        holder.text.text = currentItem.text
        holder.source.text = currentItem.source
        if (currentItem.source !== "") {
            holder.source.visibility = View.VISIBLE
            holder.sourceLbl.visibility = View.VISIBLE
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

        holder.commentImage.setOnClickListener {
            displayNotYetImplemented()
        }

        holder.retweetImage.setOnClickListener {
            displayNotYetImplemented()
        }

        holder.likeImage.setOnClickListener {
            if (!currentItem.hasUserLike) {
                currentItem.hasUserLike = true
                holder.likeCount.text = ((holder.likeCount.text as String).toInt() + 1).toString()
                holder.likeImage.setImageResource(R.drawable.ic_like_full_24)
                holder.likeImage.setColorFilter(R.color.like_color)
            } else {
                currentItem.hasUserLike = false
                holder.likeCount.text = ((holder.likeCount.text as String).toInt() - 1).toString()
                holder.likeImage.setImageResource(R.drawable.ic_like_outline_24)
                holder.likeImage.clearColorFilter()
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
}