package com.example.webnovellibrary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AccountFragment : Fragment() {

    private val TAG = "AccountFragment"

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123

    private lateinit var auth: FirebaseAuth

//    override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
////        updateUI(currentUser)
//
//        //TODO user is signed in
//        if (currentUser != null) {
//
//        }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        auth = Firebase.auth
        val user = auth.currentUser

        createRequest()

        // Inflate the layout for this fragment
        val view = updateUI(user, inflater, container, savedInstanceState)

        return view
    }

    private fun updateUI(
        user: FirebaseUser?,
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (user == null) {
            createSignInLayout(inflater, container, savedInstanceState)
        } else {
            createSignOutLayout(inflater, container, savedInstanceState)
        }
    }

    private fun createSignInLayout(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_account, container, false)
        val googleSignInButton = view.findViewById<SignInButton>(R.id.bt_google_sign_in)

        googleSignInButton.setOnClickListener {
            Log.d(TAG, "Sign in button clicked")
            signIn()
        }

        return view
    }

    private fun createSignOutLayout(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_account_sign_out, container, false)
        val signOutButton = view.findViewById<Button>(R.id.bt_sign_out)
        val emailTextView = view.findViewById<TextView>(R.id.tv_email)

        val signInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount((activity as AppCompatActivity))
        if (signInAccount != null) {
            emailTextView.text = signInAccount.email
        }

        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut() //sign out of firebase auth
            googleSignInClient.signOut() //sign out of googleSignInClient so it doesn't auto-choose same account when signing in again
            Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show()

            //return to app main screen
            returnToLibrary()
        }

        return view
    }

    private fun createRequest() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient((activity as AppCompatActivity), gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(context, "Error ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener((activity as AppCompatActivity)) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    Toast.makeText(context, "Signed in", Toast.LENGTH_SHORT).show()

                    //TODO check if saveData is diff from cloud. If so, ask if should overwrite.

                    //return to app main screen
                    returnToLibrary()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(context, "Auth failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun returnToLibrary() {
        findNavController().popBackStack()
    }


}