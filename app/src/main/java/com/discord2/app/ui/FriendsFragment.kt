package com.discord2.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.discord2.app.ChatActivity
import com.discord2.app.R
import com.discord2.app.adapters.FriendsAdapter
import com.discord2.app.models.Friend
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FriendsFragment : Fragment(R.layout.fragment_list) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: FriendsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyText = view.findViewById<TextView>(R.id.emptyText)
        emptyText.text = "Você ainda não tem amigos.\nVá na aba Adicionar!"

        adapter = FriendsAdapter(mutableListOf()) { friend ->
            val intent = Intent(requireContext(), ChatActivity::class.java)
            intent.putExtra("friendUid", friend.uid)
            intent.putExtra("friendUsername", friend.nick)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val uid = auth.currentUser?.uid ?: return

        db.collection("friends").document(uid).collection("list")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(Friend::class.java) } ?: emptyList()
                adapter.updateData(list)
                emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
    }
}
