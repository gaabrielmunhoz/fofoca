
package com.example.fofoca

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fofoca.databinding.ItemPostBinding

class PostPerfilAdapter(
    private val posts: List<Post>,
    private val aoComentar: (Post) -> Unit
) : RecyclerView.Adapter<PostPerfilAdapter.PostViewHolder>() {

    class PostViewHolder(
        val binding: ItemPostBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostViewHolder {

        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int
    ) {
        val post = posts[position]

        holder.binding.tvAutor.text = post.autor
        holder.binding.tvUsuario.text = "@${post.usuario}"
        holder.binding.tvConteudo.text = post.conteudo

        holder.binding.tvCurtidas.visibility = View.GONE
        holder.binding.tvEditar.visibility = View.GONE
        holder.binding.tvExcluir.visibility = View.GONE

        holder.binding.tvComentarios.text = "Ver comentários"
        holder.binding.tvComentarios.setOnClickListener {
            aoComentar(post)
        }
    }

    override fun getItemCount(): Int = posts.size
}
