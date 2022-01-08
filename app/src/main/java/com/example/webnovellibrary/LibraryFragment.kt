package com.example.webnovellibrary

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

import android.content.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.lang.reflect.Type
import kotlin.collections.ArrayList
import android.app.Activity
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog


class LibraryFragment : Fragment() {

    private val TAG = "LibraryFragment"

    var folders = mutableListOf<Folder>()
    lateinit var folderAdapter: FolderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        loadData()

        //set toolbar buttons; required for fragments
        setHasOptionsMenu(true)

        //set toolbar title
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.library)

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

        //setup RecyclerView for switching items
        val itemTouchHelper = ItemTouchHelper( ReorderHelperCallback(folderAdapter) )
        itemTouchHelper.attachToRecyclerView(rclView)

        return view
    }

    //adds items in menu resource file to the toolbar
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_toolbar_library, menu)
        return super.onCreateOptionsMenu(menu, menuInflater)
    }

    //does something when a menu item is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_folder -> {
                addFolderDialog()
                return true
            }
        }

        return false
    }

    private fun addFolderDialog() {
        val builder = MaterialAlertDialogBuilder((activity as AppCompatActivity), R.style.AlertDialogTheme)

        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.popup_folder_name, view as ViewGroup?, false)

        //set the title
        viewInflated.findViewById<TextView>(R.id.tv_title).text = getString(R.string.add_folder)

        // Set up the input
        val input = viewInflated.findViewById(R.id.input) as TextInputEditText

        // Specify the type of input expected
        builder.setView(viewInflated)

        //focus on folder-name EditText when Dialog is opened and open keyboard
        focusEditText(input)

        builder.setPositiveButton(android.R.string.ok,
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                closeKeyboard()

                Log.d(TAG, "new folder requested")

                addFolder(folderName = input.text.toString())
            })

        builder.setNegativeButton(android.R.string.cancel,
            DialogInterface.OnClickListener { dialog, which ->
                //cancel the dialog & close the soft keyboard
                dialog.cancel()
            })

        //close keyboard when user closes dialog without any action
        builder.setOnCancelListener { closeKeyboard() }

        val alertDialog: AlertDialog = builder.create()

        //when user clicks enter button on keyboard
        input.onDone {
            //dismiss the dialog
            alertDialog.dismiss()
            closeKeyboard()
            //add the folder
            addFolder(folderName = input.text.toString())
        }

        alertDialog.show()
    }


    private fun focusEditText(et: EditText) {
        et.requestFocus()
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun closeKeyboard() {
        val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
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
            editFolderDialog(position)
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

    private fun editFolderDialog(position: Int) {
        val builder = MaterialAlertDialogBuilder((activity as AppCompatActivity), R.style.AlertDialogTheme)

        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.popup_folder_name, view as ViewGroup?, false)

        //set the title
        viewInflated.findViewById<TextView>(R.id.tv_title).text = getString(R.string.edit_folder)

        // Set up the input
        val folderName = viewInflated.findViewById(R.id.input) as TextInputEditText

        folderName.setText(folders[position].name)

        //focus on folder-name EditText when Dialog is opened and open keyboard
        focusEditText(folderName)

        // Specify the type of input expected
        builder.setView(viewInflated)

        builder.setPositiveButton(android.R.string.ok,
            DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
                closeKeyboard()

                //update the folder name
                editDialogPositive(position, folderName)
            })

        builder.setNegativeButton(android.R.string.cancel,
            DialogInterface.OnClickListener { dialog, _ ->
                dialog.cancel()
            })

        builder.setOnCancelListener { closeKeyboard() }

        val alertDialog: AlertDialog = builder.create()

        //when user clicks enter button on keyboard
        folderName.onDone {
            //dismiss the dialog
            alertDialog.dismiss()
            closeKeyboard()
            //edit the folder
            editDialogPositive(position, folderName)
        }

        alertDialog.show()
    }

    private fun editDialogPositive(position: Int, folderName: TextInputEditText) {
        //update WebNovel data in webNovelList
        folders[position].name = folderName.text.toString()

        //make RecyclerView show updated WebNovel
        folderAdapter.notifyItemChanged(position)
    }

    fun EditText.onDone(callback: () -> Unit) {
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                callback.invoke()
                return@setOnEditorActionListener true
            }
            false
        }
    }
}