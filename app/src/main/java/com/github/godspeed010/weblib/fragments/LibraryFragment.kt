package com.github.godspeed010.weblib.fragments

import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.godspeed010.weblib.R
import com.github.godspeed010.weblib.ReorderHelperCallback
import com.github.godspeed010.weblib.adapters.FolderAdapter
import com.github.godspeed010.weblib.focusAndShowKeyboard
import com.github.godspeed010.weblib.hideKeyboard
import com.github.godspeed010.weblib.models.Folder
import com.github.godspeed010.weblib.models.FolderColor
import com.github.godspeed010.weblib.preferences.PreferencesUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText


class LibraryFragment : Fragment() {

    private val TAG = "LibraryFragment"

    var folders = mutableListOf<Folder>()
    lateinit var folderAdapter: FolderAdapter
    lateinit var guideGroup: Group

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        folders = PreferencesUtils.loadFolders(activity)

        //set toolbar buttons; required for fragments
        setHasOptionsMenu(true)

        //set toolbar title
        (activity as AppCompatActivity).supportActionBar?.title =
            resources.getString(R.string.library)

        val rclView = view.findViewById<RecyclerView>(R.id.recycler_view)

        //click listener for RecyclerView items
        val onClickListener = object : FolderAdapter.OnClickListener {
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

        //Setup and display the RecyclerView
        folderAdapter = FolderAdapter(folders, onClickListener)
        rclView.adapter = folderAdapter
        rclView.layoutManager = LinearLayoutManager(context)

        //setup RecyclerView for switching items
        val itemTouchHelper = ItemTouchHelper(ReorderHelperCallback(folderAdapter))
        itemTouchHelper.attachToRecyclerView(rclView)

        //display the click-to-add-folder guide when RecyclerView is empty
        guideGroup = view.findViewById(R.id.gp_guide_folder)
        setGuideVisibility()

        return view
    }

    //sets visibility of guideGroup based on whether RecyclerView is empty or not
    private fun setGuideVisibility() {
        if (folderAdapter.itemCount == 0)
            guideGroup.visibility = View.VISIBLE
        else
            guideGroup.visibility = View.GONE
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

    private fun addColorsToDialog(radioGroup: RadioGroup)
    {
        enumValues<FolderColor>().forEach {
            val button = RadioButton(context)
            button.apply {
                text = ""
                tag = it
                buttonTintList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_enabled)),
                    intArrayOf(ContextCompat.getColor(requireContext(), it.rgbId))
                )
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(15, 0, 15, 0)
            }
            radioGroup.addView(button)
        }
        radioGroup.check(radioGroup.getChildAt(0).id)
    }

    private fun addFolderDialog() {
        val builder = MaterialAlertDialogBuilder(
            (activity as AppCompatActivity),
            R.style.AlertDialogTheme
        )

        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.popup_folder_name, view as ViewGroup?, false)

        //set the title
        viewInflated.findViewById<TextView>(R.id.tv_title).text = getString(R.string.add_folder)

        // Set up the input
        val input = viewInflated.findViewById(R.id.input) as TextInputEditText
        val inputColor = viewInflated.findViewById(R.id.inputColor) as RadioGroup

        // Setup the folder colors
        addColorsToDialog(inputColor)

        // Specify the type of input expected
        builder.setView(viewInflated)

        //focus on folder-name EditText when Dialog is opened and open keyboard
        input.focusAndShowKeyboard()

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            hideKeyboard()

            Log.d(TAG, "new folder requested")

            val selectedButton = inputColor.findViewById<RadioButton>(inputColor.checkedRadioButtonId)

            val folderColor = selectedButton.tag as FolderColor

            Log.d(TAG, "Folder Color: " + folderColor.name)

            addFolder(folderName = input.text.toString(), folderColor = folderColor)
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            //cancel the dialog & close the soft keyboard
            dialog.cancel()
        }

        //close keyboard when user closes dialog without any action
        builder.setOnCancelListener { hideKeyboard() }

        val alertDialog: AlertDialog = builder.create()

        //when user clicks enter button on keyboard
        input.onDone {
            val bounds = Rect()
            requireView().getHitRect(bounds)

            hideKeyboard()

            val selectedButton = inputColor.findViewById<RadioButton>(inputColor.checkedRadioButtonId)

            val folderColor = selectedButton.tag as FolderColor

            // Check that the colors are visible, if they are
            // dismiss the dialog
            if (inputColor.getLocalVisibleRect(bounds)) {
                alertDialog.dismiss()
                addFolder(folderName = input.text.toString(), folderColor = folderColor)
            }
        }

        alertDialog.show()
    }

    private fun addFolder(folderName: String, folderColor: FolderColor) {
        Log.d(TAG, "adding new folder: $folderName")
        folders.add(Folder(folderName, folderColor))
        folderAdapter.notifyItemInserted(folders.size - 1)

        //if guide group is visible, make invisible.
        setGuideVisibility()
    }

    override fun onPause() {
        super.onPause()

        PreferencesUtils.saveFolders(activity, folders)

        Log.d(TAG, "there are ${folders.size} folders ")
    }

    fun showBottomSheetDialog(position: Int) {
        val bottomSheetDialog = context?.let { BottomSheetDialog(it) }
        bottomSheetDialog?.setContentView(R.layout.bottom_sheet_dialog_folder)

        // Fixes bad peekHeight in Landscape Mode
        bottomSheetDialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED

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

        //if RecyclerView is empty, make guide visible
        setGuideVisibility()
    }

    private fun editFolderDialog(position: Int) {
        val builder = MaterialAlertDialogBuilder(
            (activity as AppCompatActivity),
            R.style.AlertDialogTheme
        )

        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.popup_folder_name, view as ViewGroup?, false)

        //set the title
        viewInflated.findViewById<TextView>(R.id.tv_title).text = getString(R.string.edit_folder)

        // Set up the input
        val folderName = viewInflated.findViewById(R.id.input) as TextInputEditText
        val folderColorGroup = viewInflated.findViewById(R.id.inputColor) as RadioGroup

        // Setup the folder colors
        addColorsToDialog(folderColorGroup)

        // Set default values based on existing folder
        val radioButton = folderColorGroup.findViewWithTag<RadioButton>(folders[position].color)

        folderColorGroup.check(radioButton.id)

        folderName.setText(folders[position].name)

        //focus on folder-name EditText when Dialog is opened and open keyboard
        folderName.focusAndShowKeyboard()

        // Specify the type of input expected
        builder.setView(viewInflated)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            hideKeyboard()

            val selectedButton = folderColorGroup.findViewById<RadioButton>(folderColorGroup.checkedRadioButtonId)

            //update the folder name
            editDialogPositive(position, folderName, selectedButton.tag as FolderColor)
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }

        builder.setOnCancelListener { hideKeyboard() }

        val alertDialog: AlertDialog = builder.create()

        //when user clicks enter button on keyboard
        folderName.onDone {
            val bounds = Rect()
            requireView().getHitRect(bounds)

            hideKeyboard()

            val selectedButton = folderColorGroup.findViewById<RadioButton>(folderColorGroup.checkedRadioButtonId)

            // Check that the colors are visible, if they are
            // dismiss the dialog
            if (folderColorGroup.getLocalVisibleRect(bounds)) {
                alertDialog.dismiss()
                editDialogPositive(position = position, folderName = folderName, folderColor = selectedButton.tag as FolderColor)
            }
        }

        alertDialog.show()
    }

    private fun editDialogPositive(position: Int, folderName: TextInputEditText, folderColor: FolderColor) {
        //update WebNovel data in webNovelList
        folders[position].name = folderName.text.toString()

        folders[position].color = folderColor

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