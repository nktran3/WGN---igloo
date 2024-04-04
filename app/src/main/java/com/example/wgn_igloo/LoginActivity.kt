package com.example.wgn_igloo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wgn_igloo.databinding.ActivityLoginBinding
import com.example.wgn_igloo.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class LoginActivity: AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var  auth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient

    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("938243713874-9iik8hlaoikc60pdpv6glich1gek2a9d.apps.googleusercontent.com")
        .requestEmail()
        .build()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth= FirebaseAuth.getInstance()

        binding.buttonLogin.setOnClickListener(){
            loginUser()
        }

        binding.textViewSignUp.setOnClickListener(){
            goToSignUp()
        }

        binding.googleBtSignIn.setOnClickListener(){
            loginGoogleUser()
        }
    }

    private fun loginGoogleUser(){
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        val intent: Intent = googleSignInClient.signInIntent
        startActivity(intent)
    }

    private fun loginUser(){
        val userEmail = binding.editTextEmailAddress.text.toString()
        val userPassword = binding.editTextPassword.text.toString()

        auth.signInWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val intent= Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(applicationContext,exception.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun goToSignUp(){
        val intent= Intent(this,SignUpActivity::class.java)
        startActivity(intent)
    }
}