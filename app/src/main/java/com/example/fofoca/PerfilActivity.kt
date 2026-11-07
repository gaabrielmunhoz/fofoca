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
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import java.util.Locale
import android.text.InputType
import com.google.firebase.auth.EmailAuthProvider
import android.util.Patterns

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

        findViewById<TextView>(R.id.tvEditarPerfil).setOnClickListener {
            editarPerfil()
        }

        findViewById<TextView>(R.id.tvAlterarSenha).setOnClickListener { alterarSenha() }

        findViewById<TextView>(R.id.tvAlterarEmail).setOnClickListener { alterarEmail() }

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

                val nomePublico = documento.getString("nome") ?: ""
                val usuarioPublico = documento.getString("usuario") ?: ""

                if (nomePublico.isNotBlank() && usuarioPublico.isNotBlank()) {
                    sincronizarPerfilPublico(
                        usuarioAtual.uid,
                        nomePublico,
                        usuarioPublico
                    )
                }

            }
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao carregar perfil: ${erro.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }


    }


    override fun onResume() {
        super.onResume()

        val usuario = FirebaseAuth.getInstance().currentUser ?: return

        usuario.reload().addOnSuccessListener {

            val emailConfirmado = usuario.email ?: return@addOnSuccessListener

            findViewById<TextView>(R.id.tvEmailPerfil).text =
                emailConfirmado

            val perfilRef = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(usuario.uid)

            perfilRef.get().addOnSuccessListener { documento ->

                if (documento.exists() &&
                    documento.getString("email") != emailConfirmado) {

                    perfilRef.update("email", emailConfirmado)
                        .addOnFailureListener { erro ->
                            Toast.makeText(
                                this,
                                "Erro ao sincronizar e-mail: ${erro.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        }
    }


    private fun editarPerfil() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val campoNome = EditText(this).apply {
            hint = "Nome"
            setText(this@PerfilActivity.findViewById<TextView>(R.id.tvNomePerfil).text)
            setSingleLine(true)
        }

        val campoUsuario = EditText(this).apply {
            hint = "Nome de usuário"
            setText(
                this@PerfilActivity.findViewById<TextView>(R.id.tvUsuarioPerfil)
                    .text.toString().removePrefix("@")
            )
            setSingleLine(true)
        }

        val campos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 8)
            addView(campoNome)
            addView(campoUsuario)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar perfil")
            .setView(campos)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()

        dialog.show()

        val botaoSalvar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        botaoSalvar.setOnClickListener {

            val nome = campoNome.text.toString().trim()
            val usuario = campoUsuario.text.toString()
                .trim()
                .removePrefix("@")
                .lowercase(Locale.ROOT)

            if (nome.isBlank()) {
                campoNome.error = "Informe seu nome"
                return@setOnClickListener
            }

            if (!usuario.matches(Regex("^[a-z0-9._]{3,20}$"))) {
                campoUsuario.error = "Use 3 a 20 caracteres: letras, números, . ou _"
                return@setOnClickListener
            }

            botaoSalvar.isEnabled = false

            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .update(
                    mapOf(
                        "nome" to nome,
                        "usuario" to usuario
                    )
                )
                .addOnSuccessListener {

                    findViewById<TextView>(R.id.tvNomePerfil).text = nome
                    findViewById<TextView>(R.id.tvUsuarioPerfil).text =
                        "@$usuario"

                    sincronizarPerfilPublico(uid,nome,usuario)

                    dialog.dismiss()

                    Toast.makeText(
                        this,
                        "Perfil atualizado!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { erro ->
                    botaoSalvar.isEnabled = true

                    Toast.makeText(
                        this,
                        "Erro ao atualizar: ${erro.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }


    private fun alterarSenha() {

        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        val email = usuario.email ?: return

        val senhaAtual = EditText(this).apply {
            hint = "Senha atual"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val novaSenha = EditText(this).apply {
            hint = "Nova senha"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val confirmarSenha = EditText(this).apply {
            hint = "Confirmar nova senha"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val campos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 8)
            addView(senhaAtual)
            addView(novaSenha)
            addView(confirmarSenha)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Alterar senha")
            .setView(campos)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()

        dialog.show()

        val botaoSalvar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        botaoSalvar.setOnClickListener {

            val atual = senhaAtual.text.toString()
            val nova = novaSenha.text.toString()
            val confirmacao = confirmarSenha.text.toString()

            if (atual.isEmpty()) {
                senhaAtual.error = "Informe sua senha atual"
                return@setOnClickListener
            }

            if (nova.length < 6) {
                novaSenha.error = "Use pelo menos 6 caracteres"
                return@setOnClickListener
            }

            if (nova != confirmacao) {
                confirmarSenha.error = "As senhas não coincidem"
                return@setOnClickListener
            }

            botaoSalvar.isEnabled = false

            val credencial = EmailAuthProvider.getCredential(
                email,
                atual
            )

            usuario.reauthenticate(credencial)
                .addOnSuccessListener {

                    usuario.updatePassword(nova)
                        .addOnSuccessListener {
                            dialog.dismiss()

                            Toast.makeText(
                                this,
                                "Senha alterada com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { erro ->
                            botaoSalvar.isEnabled = true

                            Toast.makeText(
                                this,
                                "Erro ao alterar senha: ${erro.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener {
                    botaoSalvar.isEnabled = true
                    senhaAtual.error = "Senha atual incorreta ou autenticação falhou"
                }
        }
    }


    private fun alterarEmail() {

        val usuario = FirebaseAuth.getInstance().currentUser ?: return
        val emailAtual = usuario.email ?: return

        val campoEmail = EditText(this).apply {
            hint = "Novo e-mail"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setSingleLine(true)
        }

        val campoSenha = EditText(this).apply {
            hint = "Senha atual"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val campos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 8)
            addView(campoEmail)
            addView(campoSenha)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Alterar e-mail")
            .setMessage("Enviaremos uma confirmação para o novo endereço.")
            .setView(campos)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Continuar", null)
            .create()

        dialog.show()

        val botaoSalvar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        botaoSalvar.setOnClickListener {

            val novoEmail = campoEmail.text.toString()
                .trim()
                .lowercase(Locale.ROOT)

            val senha = campoSenha.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(novoEmail).matches()) {
                campoEmail.error = "Informe um e-mail válido"
                return@setOnClickListener
            }

            if (novoEmail == emailAtual.lowercase(Locale.ROOT)) {
                campoEmail.error = "Informe um e-mail diferente"
                return@setOnClickListener
            }

            if (senha.isEmpty()) {
                campoSenha.error = "Informe sua senha atual"
                return@setOnClickListener
            }

            botaoSalvar.isEnabled = false

            val credencial = EmailAuthProvider.getCredential(
                emailAtual,
                senha
            )

            usuario.reauthenticate(credencial)
                .addOnSuccessListener {

                    usuario.verifyBeforeUpdateEmail(novoEmail)
                        .addOnSuccessListener {
                            dialog.dismiss()

                            Toast.makeText(
                                this,
                                "Confira seu novo e-mail para confirmar a alteração!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener { erro ->
                            botaoSalvar.isEnabled = true

                            Toast.makeText(
                                this,
                                "Erro: ${erro.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener {
                    botaoSalvar.isEnabled = true
                    campoSenha.error = "Não foi possível confirmar sua senha"
                }
        }
    }


    private fun sincronizarPerfilPublico(
        uid: String,
        nome: String,
        usuario: String
    ) {
        val dados = mapOf(
            "uid" to uid,
            "nome" to nome,
            "usuario" to usuario
        )

        FirebaseFirestore.getInstance()
            .collection("usuarios_publicos")
            .document(uid)
            .set(dados)
            .addOnFailureListener { erro ->
                Toast.makeText(
                    this,
                    "Erro ao sincronizar perfil: ${erro.localizedMessage}",
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