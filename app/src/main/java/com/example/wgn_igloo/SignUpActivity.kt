package com.example.wgn_igloo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.example.wgn_igloo.databinding.ActivitySignUpBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.widget.Toast


private const val TAG = "SignInActivity"

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    // Firebase instance variables
    private lateinit var auth: FirebaseAuth
    //Firestore
    private lateinit var firestoreHelper: FirestoreHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This codelab uses View Binding
        // See: https://developer.android.com/topic/libraries/view-binding
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = Firebase.auth
        // Initialize FirestoreHelper
        firestoreHelper = FirestoreHelper(this)

        binding.buttonSignUp.setOnClickListener(){
            registerUser()
        }
        binding.textViewLogin.setOnClickListener(){
            goToLogin()
        }
    }

    // Gary's old implementation
//    private fun registerUser(){
//        // Initialize FirebaseAuth
//        auth = Firebase.auth
//        val userEmail = binding.editTextEmailAddress.text.toString()
//        val userPassword = binding.editTextPassword.text.toString()
//
//        auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
//            if(task.isSuccessful){
//                val intent= Intent(this,MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//        }.addOnFailureListener { exception ->
//            Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
//        }
//    }

    //implementation to check with addtional submission to the database
//    private fun registerUser() {
//        val userEmail = binding.editTextEmailAddress.text.toString()
//        val userPassword = binding.editTextPassword.text.toString()
////        // Replace editTextUsername with your actual username input field id if available
////        val username = binding.editTextUsername.text.toString()
////        if (username.isNotEmpty() && userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
//        if (userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
//            auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(this) { task ->
//                if(task.isSuccessful){
//                    // Here we get the newly created user's UID
//                    val uid = auth.currentUser?.uid
//                    if (uid != null) {
//                        // Create a new User object with the username, email, and UID
//                        val newUser = User(email = userEmail, uid = uid)
//                        // Use FirestoreHelper to add the user to Firestore
//                        firestoreHelper.addUser(newUser)
//                    }
//                    // Proceed to MainActivity
//                    val intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                } else {
//                    // If sign up fails, display a message to the user.
//                    task.exception?.message?.let { message ->
//                        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun registerUser() {
        val userEmail = binding.editTextEmailAddress.text.toString()
        val userPassword = binding.editTextPassword.text.toString()

        if (userEmail.isNotEmpty() && userPassword.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Here we get the newly created user's UID
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // Create a new User object with the email, uid, and an initialized empty list of friends
                        val newUser = User(email = userEmail, uid = uid, username = uid)
                        // Use FirestoreHelper to add the user to Firestore
                        firestoreHelper.addUser(newUser)
                    }
                    // Proceed to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign up fails, display a message to the user.
                    task.exception?.message?.let { message ->
                        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun goToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}