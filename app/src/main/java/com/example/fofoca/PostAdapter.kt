package com.example.fofoca

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fofoca.databinding.ItemPostBinding
import android.view.View
import androidx.core.content.ContextCompat
import android.content.res.ColorStateList

class PostAdapter (
    private val posts: List<Post>,
    private val uidLogado: String,
    private val aoEditar: (Post) -> Unit,
    private val aoExcluir: (Post) -> Unit,
    private val aoCurtir: (Post) -> Unit,
    private val aoComentar: (Post) -> Unit

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

        holder.binding.tvCurtidas.text =
            if (post.curtiu){ "${post.curtidas} curtidas"
        } else {
            "${post.curtidas} curtidas"
        }

        holder.binding.tvCurtidas.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                if (post.curtiu) R.color.fofoca_rosa
                else R.color.fofoca_secundario
            )
        )


        holder.binding.tvCurtidas.setCompoundDrawablesWithIntrinsicBounds(
            if (post.curtiu) R.drawable.ic_coracao_cheio
            else R.drawable.ic_coracao_vazio,
            0, 0, 0
        )

        holder.binding.tvCurtidas.compoundDrawableTintList =
            ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.context,if (post.curtiu) R.color.fofoca_rosa else R.color.fofoca_secundario))

        holder.binding.tvCurtidas.setOnClickListener { aoCurtir(post) }

        holder.binding.tvComentarios.text = "Ver comentários"
        holder.binding.tvComentarios.setOnClickListener { aoComentar(post) }

        holder.binding.tvEditar.visibility =
            if (post.autorId == uidLogado) {
                View.VISIBLE
            } else {
                View.GONE
            }

        holder.binding.tvEditar.setOnClickListener {
            if (post.autorId == uidLogado) {
                aoEditar(post)
            }
        }

        holder.binding.tvExcluir.visibility =
            if (post.autorId == uidLogado) {
                View.VISIBLE
            } else {
                View.GONE
            }

        holder.binding.tvExcluir.setOnClickListener {
            if (post.autorId == uidLogado) {
                aoExcluir(post)
            }
        }

    }

    override fun getItemCount(): Int {
        return posts.size
    }
}