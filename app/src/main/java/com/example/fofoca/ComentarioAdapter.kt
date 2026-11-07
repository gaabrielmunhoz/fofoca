package com.example.fofoca

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fofoca.databinding.ItemComentarioBinding

data class Comentario(
    val autor: String,
    val usuario: String,
    val conteudo: String
)

class ComentarioAdapter (
    private val comentarios: List<Comentario>
) : RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder>(){
    class ComentarioViewHolder(
        val binding: ItemComentarioBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val binding = ItemComentarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ComentarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comentario = comentarios[position]
        holder.binding.tvAutorComentario.text = "${comentario.autor} (@${comentario.usuario})"
        holder.binding.tvConteudoComentario.text = comentario.conteudo
    }

    override fun getItemCount(): Int {
        return comentarios.size
    }
}