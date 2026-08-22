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
        input.hint = "@handle do seu amigo (ex: user2282)"
        val button = view.findViewById<Button>(R.id.sendRequestButton)

        button.setOnClickListener {
            var targetHandle = input.text.toString().trim().lowercase()
            if (targetHandle.startsWith("@")) {
                targetHandle = targetHandle.substring(1)
            }
            if (targetHandle.isEmpty()) {
                Toast.makeText(requireContext(), "Digite o @handle do seu amigo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendFriendRequest(targetHandle, input)
        }
    }

    private fun sendFriendRequest(targetHandle: String, input: EditText) {
        val myUid = auth.currentUser?.uid ?: return

        db.collection("users")
            .whereEqualTo("handle", targetHandle)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(requireContext(), "Usuário @$targetHandle não encontrado", Toast.LENGTH_SHORT).show()
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
                        val myNick = myDoc.getString("nick") ?: "Usuário"
                        val myHandle = myDoc.getString("handle") ?: ""
                        val request = FriendRequest(
                            fromUid = myUid,
                            fromNick = myNick,
                            fromHandle = myHandle,
                            toUid = targetUid,
                            status = "pending",
                            createdAt = System.currentTimeMillis()
                        )
                        db.collection("friendRequests").add(request)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Pedido enviado!", Toast.LENGTH_SHORT).show()
                                input.text.clear()
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
