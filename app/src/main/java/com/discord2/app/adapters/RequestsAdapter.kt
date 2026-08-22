package com.discord2.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.discord2.app.R
import com.discord2.app.models.FriendRequest

class RequestsAdapter(
    private val requests: MutableList<FriendRequest>,
    private val onAccept: (FriendRequest) -> Unit,
    private val onReject: (FriendRequest) -> Unit
) : RecyclerView.Adapter<RequestsAdapter.RequestViewHolder>() {

    inner class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.nameText)
        val acceptButton: Button = view.findViewById(R.id.acceptButton)
        val rejectButton: Button = view.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.nameText.text = request.fromUsername
        holder.acceptButton.setOnClickListener { onAccept(request) }
        holder.rejectButton.setOnClickListener { onReject(request) }
    }

    override fun getItemCount() = requests.size

    fun updateData(newRequests: List<FriendRequest>) {
        requests.clear()
        requests.addAll(newRequests)
        notifyDataSetChanged()
    }
}
