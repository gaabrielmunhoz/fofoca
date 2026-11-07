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
import android.widget.EditText
import android.text.InputFilter
import androidx.appcompat.app.AlertDialog

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
        binding.rvPosts.adapter = PostAdapter(emptyList(), auth.currentUser?.uid.orEmpty(), ::editarPost, ::excluirPost,
            ::alternarCurtida, :: abrirComentarios)

        binding.tvSair.setOnClickListener {
            auth.signOut()
            voltarParaLogin()
        }

        binding.tvPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
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

                val postsComCurtidas = posts.toMutableList()
                var consultasPendentes = posts.size

                posts.forEachIndexed { indice, post ->
                    db.collection("posts")
                        .document(post.id)
                        .collection("curtidas")
                        .get()
                        .addOnSuccessListener { resultadoCurtidas ->
                            postsComCurtidas[indice] = post.copy(
                                curtidas = resultadoCurtidas.size(),
                                curtiu = resultadoCurtidas.documents.any {
                                    it.id == auth.currentUser?.uid
                                }
                            )
                        }
                        .addOnCompleteListener {
                            consultasPendentes--
                            if (consultasPendentes == 0) {
                                binding.rvPosts.adapter = PostAdapter(
                                    postsComCurtidas,
                                    auth.currentUser?.uid.orEmpty(),
                                    ::editarPost,
                                    ::excluirPost,
                                    ::alternarCurtida,
                                    ::abrirComentarios
                                )
                            }
                        }
                }

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

    private fun editarPost(post: Post){
        if(post.autorId != auth.currentUser?.uid) return

        val campo = EditText(this).apply {
            setText(post.conteudo)
            setSelection(text.length)
            filters = arrayOf(InputFilter.LengthFilter(500))
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar fofoca")
            .setView(campo)
            .setNegativeButton("Cancelar",null)
            .setPositiveButton("Salvar",null)
            .create()
        dialog.show()

        val botaoSalvar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        botaoSalvar.setOnClickListener {
            val novoTexto = campo.text.toString().trim()

            if (novoTexto.isEmpty()){
                campo.error = "A fofoca não pode ficar vazia"
                return@setOnClickListener
            }

            botaoSalvar.isEnabled = false

            db.collection("posts")
                .document(post.id)
                .update("conteudo", novoTexto)
                .addOnSuccessListener {
                    dialog.dismiss()
                    carregarPosts()

                    Toast.makeText(this, "Fofoca atualizada!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { erro ->
                    botaoSalvar.isEnabled = true

                    Toast.makeText(this, "Erro ao editar: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun excluirPost(post: Post) {
        if(post.autorId != auth.currentUser?.uid) return

        AlertDialog.Builder(this)
            .setTitle("Excluir fofoca?")
            .setMessage("Esta ação não pode ser desfeita.")
            .setNegativeButton("Cancelar",null)
            .setPositiveButton("Excluir") { _, _ ->
                db.collection("posts")
                    .document(post.id)
                    .delete()
                    .addOnSuccessListener {
                        carregarPosts()
                        Toast.makeText(this, "Fofoca excluída!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { erro->
                        Toast.makeText(this, "Erro ao excluir: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
            .show()
    }

    private fun alternarCurtida(post: Post){
        val uid = auth.currentUser?.uid ?: return

        val curtidaRef = db.collection("posts")
            .document(post.id)
            .collection("curtidas")
            .document(uid)

        curtidaRef.get().addOnSuccessListener { documento ->
            val operacao = if (documento.exists()) {
                curtidaRef.delete()
            } else {
                curtidaRef.set(mapOf("uid" to uid))
            }

            operacao.addOnSuccessListener {
                Toast.makeText(this, "Curtida atualizada!", Toast.LENGTH_SHORT).show()
                carregarPosts()
            }.addOnFailureListener { erro ->
                Toast.makeText(this, "Erro: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
            .addOnFailureListener { erro ->
                Toast.makeText(this, "Erro ao verificar curtida: ${erro.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }

    private fun abrirComentarios(post: Post){
        val intent = Intent(this, ComentariosActivity::class.java)

        intent.putExtra("POST_ID", post.id)
        startActivity(intent)
    }

    private fun voltarParaLogin(){
        val intent = Intent(this, MainActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}