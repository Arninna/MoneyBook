package com.example.moneybook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.moneybook.databinding.ActivityLoginBinding
import com.example.moneybook.databinding.ActivityRegistrationBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    /*private lateinit var email:EditText
    private lateinit var password:EditText
    private lateinit var btnLogin:Button
    private lateinit var forgotPwd :TextView
    private lateinit var signupTextview:TextView*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            login()
        }

        binding.forgetPassword.setOnClickListener{

        }

        binding.signupReg.setOnClickListener{
            val registerIntent = Intent(this,RegistrationActivity::class.java)
            startActivity(registerIntent)
        }
    }

    fun login(){
        val email = binding.emailLogin.text.toString().trim()
        val password = binding.passwordLogin.text.toString().trim()

    }
}