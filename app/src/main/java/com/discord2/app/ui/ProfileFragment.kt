package com.discord2.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.discord2.app.LoginActivity
import com.discord2.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val handleText = view.findViewById<TextView>(R.id.handleText)
        val nickInput = view.findViewById<EditText>(R.id.nickInput)
        val saveButton = view.findViewById<Button>(R.id.saveNickButton)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)

        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val handle = doc.getString("handle") ?: ""
                val nick = doc.getString("nick") ?: ""
                handleText.text = "@$handle"
                nickInput.setText(nick)
            }

        saveButton.setOnClickListener {
            val newNick = nickInput.text.toString().trim()
            if (newNick.isEmpty()) {
                Toast.makeText(requireContext(), "O nick não pode ficar vazio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users").document(uid).update("nick", newNick)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Nick atualizado!", Toast.LENGTH_SHORT).show()
                    syncNickWithFriends(uid, newNick)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    /**
     * Quando o nick muda, atualiza a cópia salva em friends/{amigo}/list/{meu-uid}
     * de cada amigo, pra eles verem o nome novo sem precisar re-adicionar.
     */
    private fun syncNickWithFriends(myUid: String, newNick: String) {
        db.collection("friends").document(myUid).collection("list").get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    val friendUid = doc.id
                    db.collection("friends").document(friendUid).collection("list")
                        .document(myUid)
                        .update("nick", newNick)
                }
            }
    }
}
