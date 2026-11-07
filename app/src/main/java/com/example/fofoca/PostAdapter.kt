package com.example.fofoca

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fofoca.databinding.ItemPostBinding
import android.view.View

class PostAdapter (
    private val posts: List<Post>,
    private val uidLogado: String

) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(
        val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        holder.binding.tvAutor.text = post.autor
        holder.binding.tvUsuario.text = "@${post.usuario}"
        holder.binding.tvConteudo.text = post.conteudo
        holder.binding.tvCurtidas.text = "${post.curtidas} curtidas"
        holder.binding.tvComentarios.text = "${post.comentarios} comentários"

        holder.binding.tvEditar.visibility =
            if (post.autorId == uidLogado) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}