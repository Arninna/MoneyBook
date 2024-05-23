package com.example.moneybook

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.moneybook.databinding.ActivityLoginBinding
import com.example.moneybook.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    /*private lateinit var email:EditText
    private lateinit var password:EditText
    private lateinit var btnLogin:Button
    private lateinit var forgotPwd :TextView
    private lateinit var signupTextview:TextView*/

    //@SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //login solo la prima volta dal download dell'app, le successive si accede già alla home
        if(firebaseAuth.currentUser != null){
            val intent = Intent(this,HomeActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.forgotPassword.setOnClickListener{
            var builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forgotpwd,null)
            val userEmail = view.findViewById<EditText>(R.id.editBox)
            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                compareEmail(userEmail)
                dialog.dismiss()
            }

            view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            if(dialog.window != null){
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()
        }

        binding.goToRegister.setOnClickListener{
            val registerIntent = Intent(this,RegistrationActivity::class.java)
            startActivity(registerIntent)
        }
    }

    // per test: mail account gioco, password: prova1234
    fun login(){
        val email = binding.emailLogin.text.toString().trim()
        val password = binding.passwordLogin.text.toString().trim()
        if(email.isNotEmpty() && password.isNotEmpty()){
            firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
                if(it.isSuccessful){
                    val intent = Intent(this,HomeActivity::class.java)
                    startActivity(intent)
                }else
                    Toast.makeText(this,"Credenziali errate", Toast.LENGTH_SHORT).show()
            }
        }else
            Toast.makeText(this,"Compila tutti i campi", Toast.LENGTH_LONG).show()
    }

    private fun compareEmail(email: EditText){
        if(email.text.toString().isEmpty())
            return
        if(!Patterns.EMAIL_ADDRESS.matcher(email.text.toString().trim()).matches()){
            return
        }
        firebaseAuth.sendPasswordResetEmail(email.text.toString().trim()).addOnCompleteListener{task ->
            if(task.isSuccessful)
                Toast.makeText(this,"Controlla le tue email",Toast.LENGTH_LONG).show()
        }
    }
}