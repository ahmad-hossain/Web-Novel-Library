package com.example.webnovellibrary

import android.app.AlertDialog
import android.content.DialogInterface
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


class LibraryFragment : Fragment() {

    private val TAG = "LibraryFragment"

    val folders = mutableListOf<Folder>()
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
                    .actionLibraryFragmentToNovelsFragment(folders[position])
                view.findNavController().navigate(action)
            }
        }

        //Setup and display the RecyclerView
        folderAdapter = FolderAdapter(folders, onClickListener)
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

    fun addFolder(folderName: String) {
        Log.d(TAG, "adding new folder: $folderName")
        folders.add( Folder(folderName) )
        folderAdapter.notifyItemInserted(folders.size - 1)
    }

    fun getData(): MutableList<String> {
        val folders = mutableListOf<String>()
        folders.add("One")
        folders.add("Two")
        folders.add("Three")
        folders.add("Something")
        folders.add("One")
        folders.add("Two")
        folders.add("Three")
        folders.add("Something")
        folders.add("One")
        folders.add("Two")
        folders.add("Three")
        folders.add("Something")
        folders.add("One")
        folders.add("Two")
        folders.add("Three")
        folders.add("Something")
        folders.add("One")
        folders.add("Two")
        folders.add("Three")
        folders.add("Something")
        folders.add("One")
        folders.add("Two")
        folders.add("Three")
        folders.add("Something")
        folders.add("One")
        folders.add("Two")
        folders.add("Three")
        folders.add("Something")

        return folders
    }
}