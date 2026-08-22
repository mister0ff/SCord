package com.discord2.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.discord2.app.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val nickInput = findViewById<EditText>(R.id.usernameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val actionButton = findViewById<Button>(R.id.actionButton)
        val toggleModeText = findViewById<TextView>(R.id.toggleModeText)

        nickInput.hint = "Nick (nome de exibição)"

        // já logado? pula direto pro app
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        toggleModeText.setOnClickListener {
            isRegisterMode = !isRegisterMode
            if (isRegisterMode) {
                nickInput.visibility = View.VISIBLE
                actionButton.text = "CADASTRAR"
                toggleModeText.text = "Já tem conta? Entrar"
            } else {
                nickInput.visibility = View.GONE
                actionButton.text = "ENTRAR"
                toggleModeText.text = "Não tem conta? Cadastre-se"
            }
        }

        actionButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isRegisterMode) {
                val nick = nickInput.text.toString().trim()
                if (nick.isEmpty()) {
                    Toast.makeText(this, "Escolha um nick", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                actionButton.isEnabled = false
                registerUser(nick, email, password, actionButton)
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun registerUser(nick: String, email: String, password: String, actionButton: Button) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                reserveUniqueHandle(uid) { handle ->
                    val user = User(uid = uid, handle = handle, nick = nick, email = email)
                    db.collection("users").document(uid).set(user)
                        .addOnSuccessListener { goToMain() }
                        .addOnFailureListener { e ->
                            actionButton.isEnabled = true
                            Toast.makeText(this, "Erro ao salvar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                actionButton.isEnabled = true
                Toast.makeText(this, "Erro ao cadastrar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Gera um handle único no formato "user1234" (fixo, usado pra adicionar amigos).
     * Reserva o handle na coleção "handles" usando uma transação, pra evitar
     * dois usuários acabarem com o mesmo handle ao mesmo tempo.
     */
    private fun reserveUniqueHandle(uid: String, attempt: Int = 0, onSuccess: (String) -> Unit) {
        if (attempt >= 15) {
            // no caso extremamente raro de 15 tentativas falharem, usa um número maior
            val handle = "user" + Random.nextInt(100000, 999999)
            db.collection("handles").document(handle).set(mapOf("uid" to uid))
                .addOnSuccessListener { onSuccess(handle) }
            return
        }

        val number = Random.nextInt(1000, 9999)
        val handle = "user$number"
        val handleRef = db.collection("handles").document(handle)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(handleRef)
            if (snapshot.exists()) {
                throw Exception("HANDLE_TAKEN")
            }
            transaction.set(handleRef, mapOf("uid" to uid))
            handle
        }.addOnSuccessListener {
            onSuccess(handle)
        }.addOnFailureListener {
            reserveUniqueHandle(uid, attempt + 1, onSuccess)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { goToMain() }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao entrar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
