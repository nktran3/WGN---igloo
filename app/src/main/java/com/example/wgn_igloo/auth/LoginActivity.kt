package com.example.wgn_igloo.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.wgn_igloo.MainActivity
import com.example.wgn_igloo.database.FirestoreHelper
import com.example.wgn_igloo.database.User
import com.example.wgn_igloo.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestoreHelper: FirestoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        configureGoogleSignIn() // Configure Google Sign-In on activity creation

        firestoreHelper = FirestoreHelper(this)

        binding.buttonLogin.setOnClickListener {
            loginUser()
        }

        binding.textViewSignUp.setOnClickListener {
            goToSignUp()
        }

        binding.googleBtSignIn.setOnClickListener {
            Log.d(TAG, "Google Button Clicked")
            signInGoogle()
        }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id))
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInGoogle() {
        configureGoogleSignIn() // Reinitialize Google Sign-In client each time before sign-in
        Log.d(TAG, "About to get signInIntent from googleSignInClient.")
        val signInIntent = googleSignInClient.signInIntent
        Log.d(TAG, "Received signInIntent: $signInIntent")
        if (signInIntent != null) {
            launcher.launch(signInIntent)
            Log.d(TAG, "Launching signIn intent.")
        } else {
            Log.e(TAG, "signInIntent was null")
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Log.d(TAG, "Sign-in activity result received with code: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "Result OK, trying to get GoogleSignInAccount...")
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleResults(task)
            } catch (e: ApiException) {
                Log.e(TAG, "Sign-in failed: ${e.statusCode} ${e.localizedMessage}", e)
            }
        } else {
            Log.e(TAG, "Sign-in was canceled or failed, result code: ${result.resultCode}")
            if (result.data != null && result.data!!.extras != null) {
                result.data!!.extras!!.keySet().forEach {
                    val value = result.data!!.extras!!.get(it)
                    Log.e(TAG, "Extra key: $it, value: $value")
                }
            }
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                // Retrieve user's email and name from the Google account
                val userEmail = account.email ?: "No email found"
                val userGivenName = account.givenName ?: "No given name found"
                val userFamilyName = account.familyName ?: "No family name found"

                firebaseAuthWithGoogle(account.idToken, userGivenName, userFamilyName,  userEmail)

                // Optionally log or use the email and name
                Log.d(TAG, "User email: $userEmail")
                Log.d(TAG, "User name: $userGivenName")



            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "Google sign-in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?, givenName: String, familyName: String, gmail:String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success, navigate to MainActivity
                    Log.d(TAG, "signInWithCredential:success")
                    Log.d(TAG, "UID: ${auth.currentUser?.uid}")

                    // Create a new User object with the email, uid, and an initialized empty list of friends
                    val newUser = auth.currentUser?.let { User(givenName = givenName, familyName = familyName, email = gmail, uid = it.uid, username = it.uid) }
                    // Use FirestoreHelper to add the user to Firestore
                    if (newUser != null) {
                        firestoreHelper.addUser(newUser)
                    }


                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // If sign-in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginUser() {
        val userEmail = binding.emailLogin.text.toString()
        val userPassword = binding.passwordLogin.text.toString()

        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }
}