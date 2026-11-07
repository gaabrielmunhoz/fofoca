package com.example.fofoca

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fofoca.databinding.ActivityFeedBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFeedBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (auth.currentUser == null){
            voltarParaLogin()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = PostAdapter(emptyList(), auth.currentUser?.uid.orEmpty())

        binding.tvSair.setOnClickListener {
            auth.signOut()
            voltarParaLogin()
        }

        binding.btnNovaFofoca.setOnClickListener {
            val intent = Intent(this, NovaFofocaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume(){
        super.onResume()
        if (auth.currentUser != null) {
            carregarPosts()
        }
    }

    private fun carregarPosts(){
        binding.tvVazio.text = "Carregando fofocas..."
        binding.tvVazio.visibility = View.VISIBLE
        binding.rvPosts.visibility = View.GONE

        db.collection("posts").orderBy("criadoEm", Query.Direction.DESCENDING)
            .get().addOnSuccessListener(this) { resultado ->
                val posts = resultado.documents.map {documento ->
                    Post(
                        id = documento.id,
                        autorId = documento.getString("autorId") ?: "",
                        autor = documento.getString("autor") ?: "",
                        usuario = documento.getString("usuario") ?: "",
                        conteudo = documento.getString("conteudo") ?: "",
                        curtidas = documento.getLong("curtidas")?.toInt() ?: 0,
                        comentarios = documento.getLong("comentarios")?.toInt() ?: 0
                    )
                }

                binding.rvPosts.adapter = PostAdapter(posts, auth.currentUser?.uid.orEmpty())

                if (posts.isEmpty()) {
                    binding.tvVazio.text = "Ainda não há fofocas aqui."
                    binding.tvVazio.visibility = View.VISIBLE
                    binding.rvPosts.visibility = View.GONE
                } else {
                    binding.tvVazio.visibility = View.GONE
                    binding.rvPosts.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener(this) { erro ->
                binding.tvVazio.text = "Não foi possível carregar as fofocas."
                binding.tvVazio.visibility = View.VISIBLE
                binding.rvPosts.visibility = View.GONE

                Toast.makeText(this, "Erro: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun voltarParaLogin(){
        val intent = Intent(this, MainActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}