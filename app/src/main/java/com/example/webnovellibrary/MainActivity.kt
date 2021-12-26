package com.example.webnovellibrary

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var startingSaveData: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set the value of startingSaveData to later check if data was changed in onStop
        loadJsonData().also {
            if (it != null) {
                startingSaveData = it
            }
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        //setup toolbar with nav to enable using UP button
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val builder = AppBarConfiguration.Builder(navController.graph)
        val appBarConfiguration = builder.build()
        toolbar.setupWithNavController(navController, appBarConfiguration)

        //enable bottom nav. buttons to move between fragments
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavView.setupWithNavController(navController)
    }

    override fun onStop() {
        super.onStop()
        
        saveDataToFirebase()
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

            val database = Firebase.database
            val myRef = database.reference


            myRef.child("Users")
                .child(user.uid)
                .setValue(currentSaveData)
        }
    }

    private fun loadJsonData(): String? {
        val sharedPreferences: SharedPreferences =
            this.getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        
        val emptyList = Gson().toJson(ArrayList<Folder>())

        val json = sharedPreferences.getString("foldersList", emptyList)

        Log.d(TAG, "loadJsonData: $json")
        
        return json
    }

}