package com.example.moneybook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.moneybook.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    /*private lateinit var  email: EditText
    private lateinit var  password: EditText
    private lateinit var  btnRegister: Button
    private lateinit var  signinTextView: TextView*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            registration()
        }
    }

    fun registration(){
        val email = binding.emailRegister.text.toString().trim()
        val password = binding.passwordRegister.text.toString().trim()
        Toast.makeText(this,email + " " + password,Toast.LENGTH_LONG).show()
    }
}