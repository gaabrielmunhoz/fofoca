package com.example.fofoca

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query

class ComentariosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comentarios)

        val postId = intent.getStringExtra("POST_ID")

        if (postId.isNullOrBlank()){
            finish()
            return
        }

        findViewById<TextView>(R.id.tvVoltar).setOnClickListener {
            finish()
        }

        findViewById<RecyclerView>(R.id.rvComentarios).layoutManager = LinearLayoutManager(this)
        carregarComentarios(postId)

        findViewById<Button>(R.id.btnEnviar).setOnClickListener { publicarComentario(postId) }

        val margem = (16 * resources.displayMetrics.density).toInt()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val teclado = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(margem + barras.left, margem + barras.top, margem + barras.right, margem + maxOf(barras.bottom, teclado.bottom))
            insets
        }
    }

    private fun publicarComentario(postId: String) {
        val campo = findViewById<EditText>(R.id.etComentario)
        val botao = findViewById<Button>(R.id.btnEnviar)

        val conteudo = campo.text.toString().trim()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "Faça login para comentar", Toast.LENGTH_SHORT).show()
            return
        }

        if (conteudo.isEmpty()){
            campo.error = "Escreva um comentário!"
            return
        }

        if (conteudo.length>300) {
            campo.error = "Máximo de 300 caracteres"
            return
        }

        botao.isEnabled = false
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { perfil->
                val nome = perfil.getString("nome")
                val usuario = perfil.getString("usuario")

                if (nome.isNullOrBlank() || usuario.isNullOrBlank()) {
                    botao.isEnabled = true
                    Toast.makeText(this, "Perfil não encontrado", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val dados = hashMapOf(
                    "autorId" to uid,
                    "autor" to nome,
                    "usuario" to usuario,
                    "conteudo" to conteudo,
                    "criadoEm" to FieldValue.serverTimestamp(),
                )

                db.collection("posts")
                    .document(postId)
                    .collection("comentarios")
                    .add(dados)
                    .addOnSuccessListener {
                        campo.text.clear()
                        carregarComentarios(postId)
                        botao.isEnabled = true
                        Toast.makeText(this, "Comentário publicado!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { erro ->
                        botao.isEnabled = true
                        Toast.makeText(this, "Erro: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { erro->
                botao.isEnabled = true
                Toast.makeText(this, "Erro ao buscar perfil: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun carregarComentarios(postId: String) {
        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(postId)
            .collection("comentarios")
            .orderBy("criadoEm", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { resultado ->
                val comentarios = resultado.documents.map {documento->
                    Comentario(
                        autor = documento.getString("autor") ?: "",
                        usuario = documento.getString("usuario") ?: "",
                        conteudo = documento.getString("conteudo") ?: "",
                    )
                }

                findViewById<RecyclerView>(R.id.rvComentarios).adapter = ComentarioAdapter(comentarios)
            }
            .addOnFailureListener { erro->
                Toast.makeText(this, "Erro ao carregar comentários: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }
}