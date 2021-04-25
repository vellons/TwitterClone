package it.uninsubria.pdm.vellons.twitterclone.tweet

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.uninsubria.pdm.vellons.twitterclone.R

class TweetAdapter(private val tweetList: List<Tweet>) :
// Useful: https://www.youtube.com/watch?v=6Gm3eMG8KqI
    RecyclerView.Adapter<TweetAdapter.TweetViewHolder>() {

    class TweetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById<TextView>(R.id.textViewName)
        val username: TextView = itemView.findViewById<TextView>(R.id.textViewUsername)
        val text: TextView = itemView.findViewById<TextView>(R.id.text)

        init {
            itemView.findViewById<ImageButton>(R.id.imageButtonLike).setOnClickListener {
                val imageButtonLike: ImageButton = itemView.findViewById(R.id.imageButtonLike)
                imageButtonLike.setImageResource(R.drawable.ic_like_full_24)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        val currentItem = tweetList[position]
        holder.name.text = currentItem.name
        holder.username.text = "@" + currentItem.username
        holder.text.text = currentItem.text
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.tweet, parent, false)
        return TweetViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return tweetList.size
    }
}