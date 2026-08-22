package com.discord2.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.discord2.app.adapters.MessagesAdapter
import com.discord2.app.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: MessagesAdapter
    private lateinit var chatId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val friendUid = intent.getStringExtra("friendUid") ?: return
        val friendUsername = intent.getStringExtra("friendUsername") ?: "Amigo"
        val myUid = auth.currentUser?.uid ?: return

        // gera um id de conversa único e igual para os dois lados
        chatId = if (myUid < friendUid) "${myUid}_$friendUid" else "${friendUid}_$myUid"

        findViewById<TextView>(R.id.chatTitle).text = friendUsername

        val recyclerView = findViewById<RecyclerView>(R.id.messagesRecyclerView)
        val messageInput = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)

        adapter = MessagesAdapter(mutableListOf(), myUid)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                adapter.updateData(list)
                if (list.isNotEmpty()) recyclerView.scrollToPosition(list.size - 1)
            }

        sendButton.setOnClickListener {
            val text = messageInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val message = Message(
                senderId = myUid,
                text = text,
                timestamp = System.currentTimeMillis()
            )

            db.collection("chats").document(chatId).collection("messages")
                .add(message)

            messageInput.text.clear()
        }
    }
}
