package com.example.wgn_igloo.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.MainActivity
import com.example.wgn_igloo.database.User
import com.example.wgn_igloo.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


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
        val userEmail = binding.emailSignup.text.toString()
        val userPassword = binding.passwordSignup.text.toString()
        val confirmUserPassword = binding.confirmPasswordSignup.text.toString()
        if (!checkUserEmail(userEmail)){
            Toast.makeText(baseContext, "Email input invalid", Toast.LENGTH_SHORT).show()
            return
        }
        if (!checkPassword(userPassword)){
            Toast.makeText(baseContext, "Password must contain an upper-case letter, numerical digit and be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }
        if (userPassword != confirmUserPassword) {
            Log.d(TAG, "Password: $userPassword")
            Log.d(TAG, "Confirm: $confirmUserPassword")
            Toast.makeText(baseContext, "Password do not match, try again", Toast.LENGTH_SHORT).show()
            return
        }

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

    private fun checkUserEmail(userEmail: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
        return userEmail.matches(emailRegex.toRegex())
    }
    private fun checkPassword(password: String): Boolean {
        val lengthRequirement = password.length >= 6
        val uppercaseRequirement = password.any { it.isUpperCase() }
        val numberRequirement = password.any { it.isDigit() }

        return lengthRequirement && uppercaseRequirement && numberRequirement
    }


    private fun goToLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

}