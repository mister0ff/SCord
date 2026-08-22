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

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val actionButton = findViewById<Button>(R.id.actionButton)
        val toggleModeText = findViewById<TextView>(R.id.toggleModeText)

        // já logado? pula direto pro app
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        toggleModeText.setOnClickListener {
            isRegisterMode = !isRegisterMode
            if (isRegisterMode) {
                usernameInput.visibility = View.VISIBLE
                actionButton.text = "CADASTRAR"
                toggleModeText.text = "Já tem conta? Entrar"
            } else {
                usernameInput.visibility = View.GONE
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
                val username = usernameInput.text.toString().trim()
                if (username.isEmpty()) {
                    Toast.makeText(this, "Escolha um nome de usuário", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                registerUser(username, email, password)
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val user = User(
                    uid = uid,
                    username = username,
                    usernameLower = username.lowercase(),
                    email = email
                )
                db.collection("users").document(uid).set(user)
                    .addOnSuccessListener { goToMain() }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao salvar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao cadastrar: ${e.message}", Toast.LENGTH_SHORT).show()
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
