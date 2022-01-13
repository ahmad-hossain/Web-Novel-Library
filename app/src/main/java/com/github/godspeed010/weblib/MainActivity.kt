package com.github.godspeed010.weblib

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var startingSaveData: String

    private lateinit var toolbar: MaterialToolbar
    private lateinit var navController: NavController
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set the value of startingSaveData to later check if data was changed in onStop
        setStartingSaveData()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //setup toolbar with nav to enable using UP button
        setupToolbarWithNav()

        //enable bottom nav. buttons to move between fragments
        setupBottomNavWithNav()

        // Obtain the FirebaseAnalytics instance.
        analytics = Firebase.analytics
    }

    override fun onStop() {
        super.onStop()

        saveDataToFirebase()
    }

    private fun setupBottomNavWithNav() {
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavView.setupWithNavController(navController)
    }

    private fun setupToolbarWithNav() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val builder = AppBarConfiguration.Builder(navController.graph)
        val appBarConfiguration = builder.build()
        toolbar.setupWithNavController(navController, appBarConfiguration)
    }

    private fun setStartingSaveData() {
        loadJsonData().also {
            if (it != null) {
                startingSaveData = it
            }
        }
    }

    private fun saveDataToFirebase() {
        val user = Firebase.auth.currentUser
        val currentSaveData = loadJsonData()

        if (user != null && startingSaveData != currentSaveData) {
            //reset the value for startingSaveData when backing up. ?May not be needed because app
            if (currentSaveData != null) {
                startingSaveData = currentSaveData
            }

            Log.d(TAG, "Found user and new save")

            val databaseRef = Firebase.database.getReference(resources.getString(R.string.fb_data_path, user.uid))

            databaseRef.setValue(currentSaveData)
        }
    }

    private fun loadJsonData(): String? {
        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        
        val emptyList = Gson().toJson(ArrayList<Folder>())

        val json = sharedPreferences.getString("foldersList", emptyList)

        return json
    }
}