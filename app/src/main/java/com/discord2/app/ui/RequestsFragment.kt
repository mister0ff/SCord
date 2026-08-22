package com.discord2.app.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.discord2.app.R
import com.discord2.app.adapters.RequestsAdapter
import com.discord2.app.models.Friend
import com.discord2.app.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RequestsFragment : Fragment(R.layout.fragment_list) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: RequestsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyText = view.findViewById<TextView>(R.id.emptyText)
        emptyText.text = "Nenhum pedido de amizade pendente."

        adapter = RequestsAdapter(
            mutableListOf(),
            onAccept = { request -> acceptRequest(request) },
            onReject = { request -> rejectRequest(request) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val uid = auth.currentUser?.uid ?: return

        db.collection("friendRequests")
            .whereEqualTo("toUid", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FriendRequest::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                adapter.updateData(list)
                emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun acceptRequest(request: FriendRequest) {
        val myUid = auth.currentUser?.uid ?: return
        val now = System.currentTimeMillis()

        db.collection("friendRequests").document(request.id)
            .update("status", "accepted")

        db.collection("friends").document(myUid).collection("list")
            .document(request.fromUid)
            .set(Friend(uid = request.fromUid, handle = request.fromHandle, nick = request.fromNick, since = now))

        db.collection("users").document(myUid).get()
            .addOnSuccessListener { myDoc ->
                val myNick = myDoc.getString("nick") ?: "Usuário"
                val myHandle = myDoc.getString("handle") ?: ""
                db.collection("friends").document(request.fromUid).collection("list")
                    .document(myUid)
                    .set(Friend(uid = myUid, handle = myHandle, nick = myNick, since = now))
            }

        Toast.makeText(requireContext(), "Pedido aceito!", Toast.LENGTH_SHORT).show()
    }

    private fun rejectRequest(request: FriendRequest) {
        db.collection("friendRequests").document(request.id)
            .update("status", "rejected")
        Toast.makeText(requireContext(), "Pedido recusado", Toast.LENGTH_SHORT).show()
    }
}
