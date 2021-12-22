package com.example.webnovellibrary

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import android.content.Context.MODE_PRIVATE

import android.R.string.no
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import android.content.Context.MODE_PRIVATE
import android.preference.PreferenceManager
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.lang.reflect.Type


class LibraryFragment : Fragment() {

    private val TAG = "LibraryFragment"

    var folders = mutableListOf<Folder>()
    lateinit var folderAdapter: FolderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        //set toolbar buttons; required for fragments
        setHasOptionsMenu(true)

        val rclView = view.findViewById<RecyclerView>(R.id.recycler_view)

        //click listener for RecyclerView items
        val onClickListener = object: FolderAdapter.OnClickListener {
            override fun onItemClicked(position: Int) {
                Log.d(TAG, "onItemClicked: clicked ${folders[position].name}")

                //navigate to NovelFragment and pass a Folder
                val action = LibraryFragmentDirections
                    .actionLibraryFragmentToNovelsFragment(folders[position], position)
                view.findNavController().navigate(action)
            }

            override fun onMoreClicked(position: Int) {
                //show bottom sheet dialog for editing or deleting folder
                showBottomSheetDialog(position)
            }
        }
        val onLongClickListener = object: FolderAdapter.OnLongClickListener {
            override fun onItemLongClicked(position: Int) {
                //TODO reorder folders
            }

        }

        //Setup and display the RecyclerView
        folderAdapter = FolderAdapter(folders, onClickListener, onLongClickListener)
        rclView.adapter = folderAdapter
        rclView.layoutManager = LinearLayoutManager(context)

        return view
    }

    //adds items in menu resource file to the toolbar
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
//        menu.clear()
        menuInflater.inflate(R.menu.menu_toolbar_library, menu)
        return super.onCreateOptionsMenu(menu, menuInflater)
    }

    //does something when a menu item is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_folder -> {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Add Folder")

                val viewInflated: View = LayoutInflater.from(context)
                    .inflate(R.layout.popup_folder_name, view as ViewGroup?, false)

                // Set up the input
                val input = viewInflated.findViewById(R.id.input) as TextInputEditText

                // Specify the type of input expected
                builder.setView(viewInflated)

                builder.setPositiveButton(android.R.string.ok,
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        val folderName = input.text.toString()
                        Log.d(TAG, "new folder requested: $folderName")

                        addFolder(folderName)

                    })

                builder.setNegativeButton(android.R.string.cancel,
                    DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                builder.show()

                return true
            }
        }

        return false
    }

    private fun addFolder(folderName: String) {
        Log.d(TAG, "adding new folder: $folderName")
        folders.add( Folder(folderName) )
        folderAdapter.notifyItemInserted(folders.size - 1)
    }

    override fun onStop() {
        super.onStop()

        saveData()

        Log.d(TAG, "stopping")
        Log.d(TAG, "there are ${folders.size} folders ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadData()
    }

    private fun saveData() {
        val sharedPreferences: SharedPreferences =
            activity!!.getSharedPreferences("shared preferences", MODE_PRIVATE)

        val editor = sharedPreferences.edit()

        val gson = Gson()

        val json: String = gson.toJson(folders)

        editor.putString("foldersList", json)

        editor.apply()
    }

    private fun loadData() {

        val sharedPreferences: SharedPreferences =
            activity!!.getSharedPreferences("shared preferences", MODE_PRIVATE)

        val gson = Gson()

        val emptyList = Gson().toJson(ArrayList<Folder>())
        val json = sharedPreferences.getString("foldersList", emptyList)

        val type: Type = object : TypeToken<ArrayList<Folder?>?>() {}.type

        folders = gson.fromJson(json, type)

        if (folders == null) {

            folders = mutableListOf<Folder>()
        }
    }

    fun updateFolder(folder: Folder, position: Int) {
        folders[position] = folder
        saveData()
    }

    fun showBottomSheetDialog(position: Int) {
        val bottomSheetDialog = context?.let { BottomSheetDialog(it) }
        bottomSheetDialog?.setContentView(R.layout.bottom_sheet_dialog_folder)

        val edit = bottomSheetDialog?.findViewById<LinearLayout>(R.id.ll_edit)
        val delete = bottomSheetDialog?.findViewById<LinearLayout>(R.id.ll_delete)


        edit?.setOnClickListener {
            Log.d(TAG, "showBottomSheetDialog: edit clicked at index $position")
            editFolder(position)
            bottomSheetDialog.dismiss()
        }
        delete?.setOnClickListener {
            Log.d(TAG, "showBottomSheetDialog: delete clicked at index $position")

            deleteFolder(position)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog?.show()
    }

    private fun deleteFolder(position: Int) {
        folders.removeAt(position)
        folderAdapter.notifyItemRemoved(position)
    }

    private fun editFolder(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit Folder")

        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.popup_folder_name, view as ViewGroup?, false)

        // Set up the input
        val folderName = viewInflated.findViewById(R.id.input) as TextInputEditText

        folderName.setText(folders[position].name)

        // Specify the type of input expected
        builder.setView(viewInflated)

        builder.setPositiveButton(android.R.string.ok,
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()

                //update WebNovel data in webNovelList
                folders[position].name = folderName.text.toString()

                //make RecyclerView show updated WebNovel
                folderAdapter.notifyItemChanged(position)
            })

        builder.setNegativeButton(android.R.string.cancel,
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

}