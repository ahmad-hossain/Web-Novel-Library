package com.github.godspeed010.weblib.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.godspeed010.weblib.Constants
import com.github.godspeed010.weblib.R
import com.github.godspeed010.weblib.databinding.FragmentAccountBinding
import com.github.godspeed010.weblib.databinding.FragmentAccountSignOutBinding
import com.github.godspeed010.weblib.getUserDataRef
import com.github.godspeed010.weblib.util.loadJsonData
import com.github.godspeed010.weblib.util.toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.lang.Exception

private const val REQUEST_CODE_GOOGLE_SIGN_IN = 123

//TODO reorder functions in chronological order
class AccountFragment : Fragment() {

    private var _bindingSignIn: FragmentAccountBinding? = null
    private val bindingSignIn get() = _bindingSignIn!!

    private var _bindingSignOut: FragmentAccountSignOutBinding? = null
    private val bindingSignOut get() = _bindingSignOut!!

    private lateinit var _googleSignInClient: GoogleSignInClient
    private lateinit var _auth: FirebaseAuth
    private lateinit var _activity: AppCompatActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _auth = Firebase.auth
        val user = _auth.currentUser
        _activity = (activity as AppCompatActivity)

        buildGoogleSignInClient()

        val view = updateUI(user, inflater, container, savedInstanceState)

        return view
    }

    private fun buildGoogleSignInClient() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        _googleSignInClient = GoogleSignIn.getClient(_activity, gso)
    }

    //update UI based on user sign-in status
    private fun updateUI(user: FirebaseUser?, inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return if (user == null) {
            createSignInLayout(inflater, container, savedInstanceState)
        } else {
            createSignOutLayout(inflater, container, savedInstanceState)
        }
    }

    private fun createSignInLayout(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _bindingSignIn = FragmentAccountBinding.inflate(inflater, container, false)
        val view = bindingSignIn.root

        bindingSignIn.btGoogleSignIn.setOnClickListener {
            handleSignInClicked()
        }

        return view
    }

    private fun handleSignInClicked() {
        Timber.i("handleSignInClicked")
        signIn()
    }

    private fun signIn() {
        val signInIntent = _googleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN)
    }

    private fun createSignOutLayout(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _bindingSignOut = FragmentAccountSignOutBinding.inflate(inflater, container, false)
        val view = bindingSignOut.root

        val signedInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(_activity)

        bindingSignOut.apply {
            tvEmail.text = signedInAccount?.email ?: ""
            btSignOut.setOnClickListener {
                handleSignOutClicked()
            }
        }

        return view
    }

    // Save before sign out
    private fun handleSignOutClicked() {
        Timber.i("handleSignOutClicked")
        saveDataToFirebase()

        FirebaseAuth.getInstance().signOut() //sign out of firebase auth
        _googleSignInClient.signOut() //sign out of googleSignInClient so it doesn't auto-choose same account when signing in again
        toast(getString(R.string.signed_out))

        //return to app main screen
        returnToLibrary()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.i("onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            //TODO move into handler func

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Timber.d("firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.e("Google sign in failed", e)
                toast(getString(R.string.error_msg, e.message))
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // Got an ID token from Google. Use it to authenticate with Firebase
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        _auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(_activity) { task ->
                if (task.isSuccessful) {
                    handleSuccessfulSignIn()
                } else {
                    handleUnsuccessfulSignIn(task.exception)
                }
            }
    }

    private fun handleSuccessfulSignIn() {
        Timber.d("Sign in SUCCESS")
        // Sign in success, update UI with the signed-in user's information
        val user = _auth.currentUser

        toast(getString(R.string.signed_in_as, user?.email))

        //Checks if local data is diff from firebase data. If so, choose whether to overwrite. Returns to libraryFragment afterwards
        if (user != null) {
            resolveDBConflicts(user.uid)
        }
    }

    private fun handleUnsuccessfulSignIn(e: Exception?) {
        Timber.e("Sign in FAILURE", e)
        toast(getString(R.string.auth_failed))
    }

    private fun returnToLibrary() {
        findNavController().popBackStack()
    }

    private fun resolveDBConflicts(userID: String) {
        //get reference to the user's data in firebase db
        val userFirebaseDataRef = getUserDataRef(userID)

        //show the progress bar. Data may take time to arrive
        setProgressBarVisibility(View.VISIBLE)

        userFirebaseDataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                handleFirebaseDataChanged(snapshot, userFirebaseDataRef, this)
            }

            override fun onCancelled(error: DatabaseError) {
                handleFirebaseDataListenerCancelled(error)
            }
        })
    }

    private fun handleFirebaseDataChanged(snapshot: DataSnapshot, userFirebaseDataRef: DatabaseReference, eventListener: ValueEventListener) {
        //get the user's json data as String
        val databaseJson = snapshot.getValue(String::class.java)
        Timber.d("onDataChange: Found data in db: $databaseJson")

        //hide the progress bar. Data has been received
        setProgressBarVisibility(View.INVISIBLE)

        //if local and cloud are conflicting
        if (databaseJson != null && databaseJson != _activity.loadJsonData()) {
            Timber.d("onDataChange: CONFLICT between local and online")
            conflictAlertDialog(databaseJson)
        } else {
            returnToLibrary()
            saveDataToFirebase()
        }

        //end the listener
        userFirebaseDataRef.removeEventListener(eventListener)
    }

    private fun handleFirebaseDataListenerCancelled(error: DatabaseError) {
        Timber.e("onCancelled", error)
    }

    private fun saveDataToFirebase() {
        val user = Firebase.auth.currentUser
        val currentSaveData = _activity.loadJsonData()

        if (user != null) {
            val database = Firebase.database
            val myRef = database.reference

            myRef.child(Constants.PATH_FIREBASE_DB_USERS)
                .child(user.uid)
                .child(Constants.PATH_FIREBASE_DB_DATA)
                .setValue(currentSaveData)
        }
    }

    private fun saveJsonData(json: String) {
        Timber.d("saveJsonData: Saving $json")
        val sharedPreferences: SharedPreferences =
            _activity.getSharedPreferences(Constants.KEY_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()

        editor.putString(Constants.KEY_FOLDERS_DATA, json)

        editor.apply()
    }

    private fun conflictAlertDialog(databaseJson: String) {
        val viewInflated = LayoutInflater.from(context)
            .inflate(R.layout.popup_data_conflict, view as ViewGroup?, false)
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle(getString(R.string.resolve_data_conflicts))
            setView(viewInflated)
            setPositiveButton(getString(R.string.local)) { dialog, _ -> handleDialogLocalClicked(dialog) }
            setNegativeButton(getString(R.string.online)) { dialog, _ -> handleDialogOnlineClicked(dialog, databaseJson) }
            setOnCancelListener { handleDialogCancelled() }
        }
        builder.show()
    }

    private fun handleDialogLocalClicked(dialog: DialogInterface) {
        Timber.d("handleDialogLocalClicked()")
        dialog.dismiss()
        returnToLibrary()
    }

    private fun handleDialogOnlineClicked(dialog: DialogInterface, databaseJson: String) {
        Timber.d("handleDialogOnlineClicked(): databaseJson = $databaseJson")
        dialog.dismiss()

        // overwrite local DB with cloud db
        saveJsonData(databaseJson)

        returnToLibrary()
    }

    private fun handleDialogCancelled() {
        Timber.d("handleDialogCancelled()")
        returnToLibrary()
    }

    private fun setProgressBarVisibility(visibility: Int) {
        bindingSignIn.pbLayout.visibility = visibility
    }

    override fun onResume() {
        super.onResume()

        //set toolbar title
        _activity.supportActionBar?.title = resources.getString(R.string.account)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingSignIn = null
        _bindingSignOut = null
    }
}