package com.example.fofoca

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fofoca.databinding.ActivityCadastroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import android.content.Intent

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCadastroBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(barras.left, barras.top, barras.right, maxOf(barras.bottom, teclado.bottom))
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnCadastrar.setOnClickListener {
            cadastrarUsuario()
        }

        binding.tvVoltarLogin.setOnClickListener {
            finish()
        }
    }

    private fun cadastrarUsuario(){
        val nome = binding.etNome.text.toString().trim()
        val usuario = binding.etUsuario.text.toString().trim().removePrefix("@").lowercase(Locale.ROOT)
        val email = binding.etEmail.text.toString().trim().lowercase(Locale.ROOT)
        val senha = binding.etSenha.text.toString()
        val confirmarSenha = binding.etConfirmarSenha.text.toString()

        if (nome.isEmpty()) {
            binding.etNome.error = "Informe seu nome"
            return
        }

        if (!usuario.matches(Regex("^[a-z0-9._]{3,20}$"))){
            binding.etUsuario.error = "Use 3 a 20 caracteres: letras, números, . ou _"
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.error = "Informe um e-mail válido"
            return
        }

        if (senha.length < 6) {
            binding.etSenha.error = "A senha deve conter pelo menos 6 caracteres."
            return
        }

        if (senha != confirmarSenha) {
            binding.etConfirmarSenha.error = "As senhas não coincidem"
            return
        }

        binding.btnCadastrar.isEnabled = false

        auth.createUserWithEmailAndPassword(email,senha).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid

                if (uid != null){
                    salvarPerfil(uid, nome, usuario, email)
                } else {
                    binding.btnCadastrar.isEnabled = true
                    mostrarMensagem("não foi possível obter o usuário")
                }
            } else {
                binding.btnCadastrar.isEnabled = true

                mostrarMensagem(
                    task.exception?.localizedMessage
                        ?: "Erro ao cadastrar usuário"
                )
            }
        }
    }

    private fun salvarPerfil(uid: String, nome: String, usuario: String, email: String) {
        val dados = hashMapOf(
            "uid" to uid,
            "nome" to nome,
            "usuario" to usuario,
            "email" to email,
            "criadoEm" to FieldValue.serverTimestamp()
        )

        db.collection("usuarios").document(uid).set(dados).addOnSuccessListener {
            mostrarMensagem("Usuário cadastrado com sucesso!")

            val intent = Intent(this, PerfilActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
            .addOnFailureListener { erro ->
                binding.btnCadastrar.isEnabled = true
                mostrarMensagem("Não foi possível salvar o perfil: ${erro.localizedMessage}")
            }
    }

    private fun mostrarMensagem(mensagem: String) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
    }
}