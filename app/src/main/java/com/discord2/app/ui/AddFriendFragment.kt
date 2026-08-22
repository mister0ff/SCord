package com.discord2.app.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.discord2.app.R
import com.discord2.app.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddFriendFragment : Fragment(R.layout.fragment_add_friend) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val input = view.findViewById<EditText>(R.id.usernameSearchInput)
        val button = view.findViewById<Button>(R.id.sendRequestButton)

        button.setOnClickListener {
            val targetUsername = input.text.toString().trim()
            if (targetUsername.isEmpty()) {
                Toast.makeText(requireContext(), "Digite um nome de usuário", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendFriendRequest(targetUsername)
        }
    }

    private fun sendFriendRequest(targetUsername: String) {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users")
            .whereEqualTo("usernameLower", targetUsername.lowercase())
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(requireContext(), "Usuário não encontrado", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val targetDoc = snapshot.documents[0]
                val targetUid = targetDoc.id

                if (targetUid == myUid) {
                    Toast.makeText(requireContext(), "Você não pode adicionar a si mesmo", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection("users").document(myUid).get()
                    .addOnSuccessListener { myDoc ->
                        val myUsername = myDoc.getString("username") ?: "Usuário"
                        val request = FriendRequest(
                            fromUid = myUid,
                            fromUsername = myUsername,
                            toUid = targetUid,
                            status = "pending",
                            createdAt = System.currentTimeMillis()
                        )
                        db.collection("friendRequests").add(request)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Pedido enviado!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao buscar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

