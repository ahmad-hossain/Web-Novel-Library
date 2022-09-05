package com.github.godspeed010.weblib.fragments

import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.godspeed010.weblib.R
import com.github.godspeed010.weblib.adapters.FolderAdapter
import com.github.godspeed010.weblib.databinding.FragmentLibraryBinding
import com.github.godspeed010.weblib.databinding.PopupFolderNameBinding
import com.github.godspeed010.weblib.models.Folder
import com.github.godspeed010.weblib.models.FolderColor
import com.github.godspeed010.weblib.util.PreferencesUtils
import com.github.godspeed010.weblib.util.ReorderHelperCallback
import com.github.godspeed010.weblib.util.focusAndShowKeyboard
import com.github.godspeed010.weblib.util.hideKeyboard
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import timber.log.Timber

class LibraryFragment : Fragment() {

    private var binding: FragmentLibraryBinding? = null
    private val _binding get() = binding!!

    private var _folders = mutableListOf<Folder>()
    private lateinit var _folderAdapter: FolderAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        val view = _binding.root

        _folders = PreferencesUtils.loadFolders(activity)

        //set toolbar buttons; required for fragments
        setHasOptionsMenu(true)

        //set toolbar title
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.library)

        //click listener for RecyclerView items
        val onClickListener = object : FolderAdapter.OnClickListener {
            override fun onItemClicked(position: Int) {
                Timber.d("onItemClicked: clicked ${_folders[position].name}")

                //navigate to NovelFragment and pass a Folder
                val action = LibraryFragmentDirections
                    .actionLibraryFragmentToNovelsFragment(_folders[position], position)
                view.findNavController().navigate(action)
            }

            override fun onMoreClicked(position: Int) {
                //show bottom sheet dialog for editing or deleting folder
                showBottomSheetDialog(position)
            }
        }

        //Setup and display the RecyclerView
        _folderAdapter = FolderAdapter(_folders, onClickListener)
        _binding.recyclerView.adapter = _folderAdapter
        _binding.recyclerView.layoutManager = LinearLayoutManager(context)

        //setup RecyclerView for switching items
        val itemTouchHelper = ItemTouchHelper(ReorderHelperCallback(_folderAdapter))
        itemTouchHelper.attachToRecyclerView(_binding.recyclerView)

        //display the click-to-add-folder guide when RecyclerView is empty
        setGuideVisibility()

        return view
    }

    //sets visibility of guideGroup based on whether RecyclerView is empty or not
    private fun setGuideVisibility() {
        if (_folderAdapter.itemCount == 0)
            _binding.gpGuideFolder.visibility = View.VISIBLE
        else
            _binding.gpGuideFolder.visibility = View.GONE
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

    private fun addColorsToDialog(radioGroup: RadioGroup) {
        enumValues<FolderColor>().forEach {
            val button = RadioButton(context)
            button.apply {
                text = ""
                tag = it
                buttonTintList = ColorStateList(
                    arrayOf(intArrayOf(android.R.attr.state_enabled)),
                    intArrayOf(ContextCompat.getColor(requireContext(), it.rgbId))
                )
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
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

        // Set up the etFolderName
        val etFolderName = viewInflated.findViewById(R.id.et_folder_name) as TextInputEditText
        val inputColor = viewInflated.findViewById(R.id.rg_colors) as RadioGroup

        // Setup the folder colors
        addColorsToDialog(inputColor)

        // Specify the type of etFolderName expected
        builder.setView(viewInflated)

        //focus on folder-name EditText when Dialog is opened and open keyboard
        etFolderName.focusAndShowKeyboard()

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            hideKeyboard()

            Timber.d("new folder requested")

            val selectedButton = inputColor.findViewById<RadioButton>(inputColor.checkedRadioButtonId)

            val folderColor = selectedButton.tag as FolderColor

            Timber.d("Folder Color: " + folderColor.name)

            addFolder(folderName = etFolderName.text.toString(), folderColor = folderColor)
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            //cancel the dialog & close the soft keyboard
            dialog.cancel()
        }

        //close keyboard when user closes dialog without any action
        builder.setOnCancelListener { hideKeyboard() }

        val alertDialog: AlertDialog = builder.create()

        //when user clicks enter button on keyboard
        etFolderName.onDone {
            val bounds = Rect()
            requireView().getHitRect(bounds)

            hideKeyboard()

            val selectedButton = inputColor.findViewById<RadioButton>(inputColor.checkedRadioButtonId)

            val folderColor = selectedButton.tag as FolderColor

            // Check that the colors are visible, if they are
            // dismiss the dialog
            if (inputColor.getLocalVisibleRect(bounds)) {
                alertDialog.dismiss()
                addFolder(folderName = etFolderName.text.toString(), folderColor = folderColor)
            }
        }

        alertDialog.show()
    }

    private fun addFolder(folderName: String, folderColor: FolderColor) {
        Timber.d("adding new folder: $folderName")
        _folders.add(Folder(folderName, folderColor))
        _folderAdapter.notifyItemInserted(_folders.size - 1)

        //if guide group is visible, make invisible.
        setGuideVisibility()
    }

    override fun onStop() {
        super.onStop()

        PreferencesUtils.saveFolders(activity, _folders)

        Timber.d("stopping")
        Timber.d("there are ${_folders.size} folders ")
    }

    fun showBottomSheetDialog(position: Int) {
        val bottomSheetDialog = context?.let { BottomSheetDialog(it) }
        bottomSheetDialog?.setContentView(R.layout.bottom_sheet_dialog_folder)

        // Fixes bad peekHeight in Landscape Mode
        bottomSheetDialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED

        val edit = bottomSheetDialog?.findViewById<LinearLayout>(R.id.ll_edit)
        val delete = bottomSheetDialog?.findViewById<LinearLayout>(R.id.ll_delete)

        edit?.setOnClickListener {
            Timber.d("showBottomSheetDialog: edit clicked at index $position")
            editFolderDialog(position)
            bottomSheetDialog.dismiss()
        }
        delete?.setOnClickListener {
            Timber.d("showBottomSheetDialog: delete clicked at index $position")

            deleteFolder(position)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog?.show()
    }

    private fun deleteFolder(position: Int) {
        _folders.removeAt(position)
        _folderAdapter.notifyItemRemoved(position)

        //if RecyclerView is empty, make guide visible
        setGuideVisibility()
    }

    private fun editFolderDialog(position: Int) {
        val builder = MaterialAlertDialogBuilder((activity as AppCompatActivity), R.style.AlertDialogTheme)
        val dialogBinding = PopupFolderNameBinding.inflate(layoutInflater)
        builder.setView(dialogBinding.root)

        dialogBinding.apply {
            tvTitle.text = getString(R.string.edit_folder)
            etFolderName.apply {
                setText(_folders[position].name)
                focusAndShowKeyboard()
            }
        }

        // Setup the folder colors
        addColorsToDialog(dialogBinding.rgColors)

        // Set default values based on existing folder
        val radioButton: RadioButton = dialogBinding.rgColors.findViewWithTag(_folders[position].color)
        dialogBinding.rgColors.check(radioButton.id)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
            hideKeyboard()

            //update the folder name
            editDialogPositive(position, dialogBinding.etFolderName, dialogBinding.rgColors.selectedButton().tag as FolderColor)
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.setOnCancelListener { hideKeyboard() }

        val alertDialog: AlertDialog = builder.create()

        //when user clicks enter button on keyboard
        dialogBinding.etFolderName.onDone {
            val bounds = Rect()
            requireView().getHitRect(bounds)
            hideKeyboard()

            // Check that the colors are visible, if they are
            // dismiss the dialog
            if (dialogBinding.rgColors.getLocalVisibleRect(bounds)) {
                alertDialog.dismiss()
                editDialogPositive(position = position, folderName = dialogBinding.etFolderName, folderColor = dialogBinding.rgColors.selectedButton().tag as FolderColor)
            }
        }

        alertDialog.show()
    }

    private fun RadioGroup.selectedButton(): RadioButton {
        return findViewById(checkedRadioButtonId)
    }

    private fun editDialogPositive(position: Int, folderName: TextInputEditText, folderColor: FolderColor) {
        //update WebNovel data in webNovelList
        _folders[position].apply {
            name = folderName.text.toString()
            color = folderColor
        }

        //make RecyclerView show updated WebNovel
        _folderAdapter.notifyItemChanged(position)
    }

    private fun EditText.onDone(callback: () -> Unit) {
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                callback.invoke()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}