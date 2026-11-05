package com.example.fofoca

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fofoca.databinding.ActivityFeedBinding
import com.google.firebase.auth.FirebaseAuth

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFeedBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null){
            voltarParaLogin()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //publicacoes temporárias
        val posts = listOf(
            Post(
                id = "1",
                autor = "Ana Costa",
                usuario = "anacosta",
                conteudo = "Gente, vocês não sabem o que aconteceuhoje na faculdade...",
                curtidas = 24,
                comentarios = 8
            ),
            Post(
                id = "2",
                autor = "Pedro Lima",
                usuario = "pedrolima",
                conteudo = "Eu prometi que eu não ia me envolver nessa fofoca, mas preciso contar uma coisa.",
                curtidas = 15,
                comentarios = 3
            ),
            Post(
                id = "3",
                autor = "Marina Souza",
                usuario = "marinasouza",
                conteudo = "Alguém mais esperando o fim do semestre? Porque eu já estou para enlouquecer!.",
                curtidas = 42,
                comentarios = 12
            )
        )

        binding.rvPosts.layoutManager = LinearLayoutManager(this)
        binding.rvPosts.adapter = PostAdapter(posts)
        binding.tvVazio.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE

        binding.tvSair.setOnClickListener {
            auth.signOut()
            voltarParaLogin()
        }

        binding.btnNovaFofoca.setOnClickListener {
            val intent = Intent(this, NovaFofocaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun voltarParaLogin(){
        val intent = Intent(this, MainActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}