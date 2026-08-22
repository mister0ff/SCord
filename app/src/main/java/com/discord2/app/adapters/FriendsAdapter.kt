package com.discord2.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.discord2.app.R
import com.discord2.app.models.Friend

class FriendsAdapter(
    private val friends: MutableList<Friend>,
    private val onChatClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.nameText)
        val actionButton: Button = view.findViewById(R.id.actionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.nameText.text = "${friend.nick} (@${friend.handle})"
        holder.actionButton.setOnClickListener { onChatClick(friend) }
    }

    override fun getItemCount() = friends.size

    fun updateData(newFriends: List<Friend>) {
        friends.clear()
        friends.addAll(newFriends)
        notifyDataSetChanged()
    }
}
