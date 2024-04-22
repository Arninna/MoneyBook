package com.example.moneybook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.moneybook.databinding.ActivityRegistrationBinding
import com.google.firebase.auth.FirebaseAuth

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    /*private lateinit var  email: EditText
    private lateinit var  password: EditText
    private lateinit var  btnRegister: Button
    private lateinit var  signinTextView: TextView*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            registration()
        }

        binding.signinTextview.setOnClickListener{
            val signinIntent = Intent(this,LoginActivity::class.java)
            startActivity(signinIntent)
        }
    }

    fun registration(){
        val email = binding.emailRegister.text.toString().trim()
        val password = binding.passwordRegister.text.toString().trim()
        if(email.isNotEmpty() && password.isNotEmpty()){
            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                if(it.isSuccessful){
                    val intent = Intent(this,LoginActivity::class.java)
                    startActivity(intent)
                }else
                    Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }else
            Toast.makeText(this,"Tutti i campi devono essere completi",Toast.LENGTH_LONG).show()
    }
}