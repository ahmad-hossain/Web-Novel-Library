package com.github.godspeed010.weblib

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.github.godspeed010.weblib.databinding.ActivityMainBinding
import com.github.godspeed010.weblib.models.Folder
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import timber.log.Timber

private const val KEY_FOLDERS_DATA = "foldersList"

class MainActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityMainBinding
    private lateinit var _startingSaveData: String
    private lateinit var _navController: NavController
    private lateinit var _analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        //set the value of startingSaveData to later check if data was changed in onStop
        setStartingSaveData()

        setupUi()

        // Obtain the FirebaseAnalytics instance.
        _analytics = Firebase.analytics
    }

    /**
     * Saves the DB data from when the app is first started into the member variable [_startingSaveData]
     */
    private fun setStartingSaveData() {
        loadJsonData().also {
            if (it != null) {
                _startingSaveData = it
            }
        }
    }

    private fun loadJsonData(): String? {
        val sharedPreferences: SharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)

        val emptyList = Gson().toJson(ArrayList<Folder>())

        val json = sharedPreferences.getString(KEY_FOLDERS_DATA, emptyList)

        return json
    }

    private fun setupUi() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        _navController = navHostFragment.navController

        setSupportActionBar(_binding.toolbar)

        //setup toolbar with nav to enable using UP button
        setupToolbarWithNav()

        //enable bottom nav. buttons to move between fragments
        _binding.bottomNav.setupWithNavController(_navController)
    }

    private fun setupToolbarWithNav() {
        val builder = AppBarConfiguration.Builder(_navController.graph)
        val appBarConfiguration = builder.build()
        _binding.toolbar.setupWithNavController(_navController, appBarConfiguration)
    }

    override fun onStop() {
        super.onStop()

        saveDataToFirebase()
    }

    /**
     * If the local DB has changed since the app start or the last Firebase sync, updates the Firebase DB for the user
     */
    private fun saveDataToFirebase() {
        Timber.i("saveDataToFirebase")
        val user = Firebase.auth.currentUser
        val currentSaveData = loadJsonData()

        if (user == null || _startingSaveData == currentSaveData) return
        Timber.i("saveDataToFirebase: DB has changed since app start, so updating Firebase DB")

        //reset the value for startingSaveData when backing up. ?May not be needed because app
        if (currentSaveData != null) {
            _startingSaveData = currentSaveData
        }

        val userFirebaseDataRef = getUserDataRef(user.uid)
        userFirebaseDataRef.setValue(currentSaveData)
    }

}

fun getUserDataRef(userId: String): DatabaseReference {
    val userDataPath = String.format("%s/%s/%s", Constants.PATH_FIREBASE_DB_USERS, userId, Constants.PATH_FIREBASE_DB_DATA)
    return Firebase.database.getReference(userDataPath)
}
