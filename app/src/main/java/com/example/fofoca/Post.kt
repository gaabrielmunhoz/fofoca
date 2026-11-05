package com.example.fofoca

data class Post(
    val id: String = "",
    val autorId: String = "",
    val autor: String = "",
    val usuario: String = "",
    val conteudo: String = "",
    val curtidas: Int = 0,
    val comentarios: Int = 0
)