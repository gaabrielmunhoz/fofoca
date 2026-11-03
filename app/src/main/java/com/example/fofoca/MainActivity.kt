package com.example.fofoca

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fofoca.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        binding.tvCadastrar.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }

        binding.btnEntrar.setOnClickListener {
            realizarLogin()
        }
    }

    private fun realizarLogin(){
        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.error = "Informe um e-mail válido"
            return
        }
        if (senha.isEmpty()){
            binding.etSenha.error = "Informe a sua senha"
            return
        }

        binding.btnEntrar.isEnabled = false

        auth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(this) {task ->
            binding.btnEntrar.isEnabled = true

            if (task.isSuccessful){
                Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_LONG).show()

                val intent = Intent(this, FeedActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "não foi possível entrar. Verifique seu e-mail e senha.", Toast.LENGTH_LONG).show()
            }
        }
    }
}