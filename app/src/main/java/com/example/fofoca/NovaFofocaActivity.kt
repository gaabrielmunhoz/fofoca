package com.example.fofoca

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fofoca.databinding.ActivityNovaFofocaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NovaFofocaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNovaFofocaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNovaFofocaBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnPublicar.setOnClickListener {
            publicarFofoca()
        }

        binding.tvCancelar.setOnClickListener {
            finish()
        }
    }

    private fun publicarFofoca(){
        val conteudo = binding.etConteudo.text.toString().trim()
        val usuarioAtual = auth.currentUser

        if (conteudo.isEmpty()){
            binding.etConteudo.error = "Escreva alguma coisa!"
            return
        }

        if (conteudo.length > 500) {
            binding.etConteudo.error = "Máximo de 500 caracteres"
            return
        }

        if (usuarioAtual == null) {
            mostrarMensagem("Faça login para publicar")
            finish()
            return
        }

        binding.btnPublicar.isEnabled = false

        val uid = usuarioAtual.uid

        db.collection("usuarios").document(uid).get().addOnSuccessListener { documento ->
            if(!documento.exists()){
                binding.btnPublicar.isEnabled = true
                mostrarMensagem("Perfil não encontrado")
                return@addOnSuccessListener
            }

            val nome = documento.getString("nome")
            val usuario = documento.getString("usuario")

            if (nome.isNullOrBlank() || usuario.isNullOrBlank()){
                binding.btnPublicar.isEnabled = true
                mostrarMensagem("Dados do perfil incompetos")
                return@addOnSuccessListener
            }

            salvarPost(
                uid = uid,
                nome = nome,
                usuario = usuario,
                conteudo = conteudo
            )
            }.addOnFailureListener { erro ->
                binding.btnPublicar.isEnabled = true
                mostrarMensagem("Erro ao buscar perfil: ${erro.localizedMessage}")
            }
        }

        private fun salvarPost(
            uid: String,
            nome: String,
            usuario: String,
            conteudo: String
        ) {
            val novoPost = db.collection("posts").document()

            val dados = hashMapOf(
                "id" to novoPost.id,
                "autorId" to uid,
                "autor" to nome,
                "usuario" to usuario,
                "conteudo" to conteudo,
                "curtidas" to 0,
                "comentarios" to 0,
                "criadoEm" to FieldValue.serverTimestamp()
            )

            novoPost.set(dados).addOnSuccessListener {
                mostrarMensagem("Fofoca publicada com sucesso!")
                finish()
            }
                .addOnFailureListener { erro ->
                    binding.btnPublicar.isEnabled = true
                    mostrarMensagem("Erro ao publicar: ${erro.localizedMessage}")
                }
        }

        private fun mostrarMensagem(mensagem: String) {
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show()
        }
}