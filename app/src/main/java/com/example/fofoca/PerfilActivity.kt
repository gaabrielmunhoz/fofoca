package com.example.fofoca

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        findViewById<TextView>(R.id.tvVoltarPerfil).setOnClickListener {
            finish()
        }

        val usuarioAtual = FirebaseAuth.getInstance().currentUser

        if (usuarioAtual == null) {
            finish()
            return
        }

        findViewById<TextView>(R.id.tvEmailPerfil).text =
            usuarioAtual.email ?: ""
        findViewById<RecyclerView>(R.id.rvPostsPerfil).layoutManager=
            LinearLayoutManager(this)
        carregarPostsPerfil(usuarioAtual.uid)

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(usuarioAtual.uid)
            .get()
            .addOnSuccessListener { documento ->

                findViewById<TextView>(R.id.tvNomePerfil).text =
                    documento.getString("nome") ?: "Nome não informado"

                findViewById<TextView>(R.id.tvUsuarioPerfil).text =
                    "@${documento.getString("usuario") ?: ""}"
            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao carregar perfil: ${erro.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }


    }
    private fun carregarPostsPerfil(uid: String) {

        FirebaseFirestore.getInstance()
            .collection("posts")
            .whereEqualTo("autorId", uid)
            .get()
            .addOnSuccessListener { resultado ->

                val posts = resultado.documents
                    .sortedByDescending {
                        it.getTimestamp("criadoEm")?.seconds ?: 0L
                    }
                    .map { documento ->
                        Post(
                            id = documento.id,
                            autorId = documento.getString("autorId") ?: "",
                            autor = documento.getString("autor") ?: "",
                            usuario = documento.getString("usuario") ?: "",
                            conteudo = documento.getString("conteudo") ?: ""
                        )
                    }

                findViewById<TextView>(R.id.tvSemPostsPerfil).visibility =
                    if (posts.isEmpty()) View.VISIBLE else View.GONE

                findViewById<RecyclerView>(R.id.rvPostsPerfil).adapter =
                    PostPerfilAdapter(posts) { post ->

                        val intent = Intent(
                            this,
                            ComentariosActivity::class.java
                        )

                        intent.putExtra("POST_ID", post.id)
                        startActivity(intent)
                    }
            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao carregar publicações: ${erro.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

}