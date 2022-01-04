package com.example.webnovellibrary

import android.R.attr.label
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class NovelsFragment : Fragment() {

    private val args by navArgs<NovelsFragmentArgs>()

    private val TAG = "NovelsFragment"
    lateinit var webNovelsList: MutableList<WebNovel>
    lateinit var novelsAdapter: NovelsAdapter

    lateinit var folder: Folder
    lateinit var folderList: MutableList<Folder>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_novels, container, false)

        setHasOptionsMenu(true)

        folderList = loadData()
        folder = folderList[args.position]
//        val folder = args.folder

        //set the toolbar title to the folder name
        (activity as MainActivity).supportActionBar?.title = folder.name

//        val folder = NovelsFragmentArgs.fromBundle(requireArguments()).folder
        webNovelsList = folder.webNovels

        Log.d(TAG, "opened folder with name: ${folder.name} and ${webNovelsList.size} novels")

        // populate RecyclerView
        val rclView = view.findViewById<RecyclerView>(R.id.recycler_view2)

        //click listener for RecyclerView items
        val onClickListener = object: NovelsAdapter.OnClickListener {
            override fun onItemClicked(position: Int) {
                Log.d(TAG, "onItemClicked: clicked item $position")

                val action = NovelsFragmentDirections.
                    actionNovelsFragmentToWebViewFragment(url = webNovelsList[position].url, novelPosition = position, folderPosition = args.position)
                view.findNavController().navigate(action)

            }

            override fun onCopyClicked(position: Int) {
                Log.d(TAG, "onCopyClicked: clicked copy for index $position")

                copyToClipboard(webNovelsList[position].url)

                Toast.makeText(context, "Copied URL!", Toast.LENGTH_SHORT).show()
            }

            override fun onMoreClicked(position: Int) {
                Log.d(TAG, "onMoreClicked: clicked more for index $position")
                showBottomSheetDialog(position)
            }
        }

        //Setup and display the RecyclerView
        novelsAdapter = NovelsAdapter(webNovelsList, onClickListener)
        rclView.adapter = novelsAdapter
        rclView.layoutManager = LinearLayoutManager(context)

        //setup RecyclerView for switching items
        val itemTouchHelper = ItemTouchHelper( ReorderHelperCallback(novelsAdapter) )
        itemTouchHelper.attachToRecyclerView(rclView)

        return view
    }

    private fun copyToClipboard(text: String) {
        val clipboard: ClipboardManager? =
            context?.let { getSystemService(it, ClipboardManager::class.java) }
        val clip = ClipData.newPlainText(label.toString(), text)
        clipboard?.setPrimaryClip(clip)
    }

    //adds items in menu resource file to the toolbar
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_toolbar_novels, menu)
        return super.onCreateOptionsMenu(menu, menuInflater)
    }

    //does something when a menu item is selected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_webNovel -> {
                val builder = MaterialAlertDialogBuilder((activity as AppCompatActivity), R.style.AlertDialogTheme)

                val viewInflated: View = LayoutInflater.from(context)
                    .inflate(R.layout.popup_web_novel, view as ViewGroup?, false)

                // Set up the input
                val webNovelTitle = viewInflated.findViewById(R.id.et_webNovel_name) as TextInputEditText
                val webNovelUrl = viewInflated.findViewById(R.id.et_webNovel_url) as TextInputEditText
                val urlTextLayout = viewInflated.findViewById(R.id.tl_url) as TextInputLayout

                // Specify the type of input expected
                builder.setView(viewInflated)

                //focus on webNovelTitle EditText when Dialog is opened and open keyboard
                focusEditText(webNovelTitle)

                //Set click listener for webNovelUrl paste button
                urlTextLayout.setEndIconOnClickListener {
                    webNovelUrl.setText(clipboardPaste())
                }

                builder.setPositiveButton(android.R.string.ok,
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()

                        closeKeyboard()

                        val title = webNovelTitle.text.toString()
                        val url = webNovelUrl.text.toString()

                        Log.d(TAG, "new web novel requested: $title")

                        addNovel(title, url)

                    })

                builder.setNegativeButton(android.R.string.cancel,
                    DialogInterface.OnClickListener { dialog, which ->
                        closeKeyboard()
                        dialog.cancel()
                    })

                builder.setOnCancelListener { closeKeyboard() }

                builder.show()

                return true
            }
        }

        return false
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

    fun addNovel(title: String, url: String) {
        Log.d(TAG, "adding new webNovel $title with url: $url")
        webNovelsList.add( WebNovel(title, url) )
        novelsAdapter.notifyItemInserted(webNovelsList.size - 1)
    }

    override fun onStop() {
        super.onStop()

        Log.d(TAG, "onStop: previous novels num: ${args.folder.webNovels.size}")
        args.folder.webNovels = webNovelsList
        Log.d(TAG, "onStop: new novels num: ${args.folder.webNovels.size}")

        //load the old folders data
        val oldFolders = folderList

        //update the current folder
        oldFolders[args.position] = args.folder

        //save the new data
        saveData(oldFolders)

    }
    
    fun loadData(): MutableList<Folder> {

        val sharedPreferences: SharedPreferences =
            activity!!.getSharedPreferences("shared preferences", Context.MODE_PRIVATE)

        val gson = Gson()

        val emptyList = Gson().toJson(ArrayList<Folder>())
        val json = sharedPreferences.getString("foldersList", emptyList)

        val type: Type = object : TypeToken<ArrayList<Folder?>?>() {}.type

        var oldFolders: MutableList<Folder> = gson.fromJson(json, type)

        if (oldFolders == null) {

            oldFolders = mutableListOf<Folder>()
        }

        return oldFolders
    }

    fun saveData(folders: MutableList<Folder>) {
        val sharedPreferences: SharedPreferences =
            activity!!.getSharedPreferences("shared preferences", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()

        val gson = Gson()

        val json: String = gson.toJson(folders)

        editor.putString("foldersList", json)

        editor.apply()
    }

    fun showBottomSheetDialog(position: Int) {
        val bottomSheetDialog = context?.let { BottomSheetDialog(it) }
        bottomSheetDialog?.setContentView(R.layout.bottom_sheet_dialog_novel)

        val move = bottomSheetDialog?.findViewById<LinearLayout>(R.id.ll_move)
        val edit = bottomSheetDialog?.findViewById<LinearLayout>(R.id.ll_edit)
        val delete = bottomSheetDialog?.findViewById<LinearLayout>(R.id.ll_delete)

        move?.setOnClickListener {
            Log.d(TAG, "showBottomSheetDialog: move clicked at $position")
            showFoldersBottomSheetDialog(position)
            bottomSheetDialog.dismiss()
        }
        edit?.setOnClickListener {
            Log.d(TAG, "showBottomSheetDialog: edit clicked at index $position")
            editNovel(position)
            bottomSheetDialog.dismiss()
        }
        delete?.setOnClickListener {
            Log.d(TAG, "showBottomSheetDialog: delete clicked at index $position")

            deleteNovel(position)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog?.show()
    }

    fun editNovel(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit Web Novel")

        val viewInflated: View = LayoutInflater.from(context)
            .inflate(R.layout.popup_web_novel, view as ViewGroup?, false)

        // Set up the input
        val webNovelTitle = viewInflated.findViewById(R.id.et_webNovel_name) as TextInputEditText
        val webNovelUrl = viewInflated.findViewById(R.id.et_webNovel_url) as TextInputEditText
        val urlTextLayout = viewInflated.findViewById(R.id.tl_url) as TextInputLayout

        webNovelTitle.setText(webNovelsList[position].title)
        webNovelUrl.setText(webNovelsList[position].url)

        // Specify the type of input expected
        builder.setView(viewInflated)

        //Set click listener for webNovelUrl paste button
        urlTextLayout.setEndIconOnClickListener {
            webNovelUrl.setText(clipboardPaste())
        }

        builder.setPositiveButton(android.R.string.ok,
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()

                //update WebNovel data in webNovelList
                webNovelsList[position].title = webNovelTitle.text.toString()
                webNovelsList[position].url = webNovelUrl.text.toString()


                //make RecyclerView show updated WebNovel
                novelsAdapter.notifyItemChanged(position)
            })

        builder.setNegativeButton(android.R.string.cancel,
            DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }

    private fun clipboardPaste(): String {
        val clipboard = (context?.getSystemService(Context.CLIPBOARD_SERVICE)) as? ClipboardManager
        val textToPaste = clipboard?.primaryClip?.getItemAt(0)?.text
        return textToPaste.toString()
    }

    fun deleteNovel(position: Int) {
        webNovelsList.removeAt(position)
        novelsAdapter.notifyItemRemoved(position)
    }

    private fun showFoldersBottomSheetDialog(novelPosition: Int) {
        val bottomSheetDialog = context?.let { BottomSheetDialog(it) }
        bottomSheetDialog?.setContentView(R.layout.bottom_sheet_dialog_folder_list)

        //get String list of all folder names
        val folderNames: MutableList<String> = getFolderNames()

        //Setup onClickListener for folder items.
        val onClickListener = object: MoveNovelAdapter.OnClickListener {
            override fun onItemClicked(position: Int) {
                //Move novel to desired folder
                changeNovelFolder(novelPosition, position)

                bottomSheetDialog?.dismiss()

                Toast.makeText(context, "Moved novel to ${folderNames[position]}", Toast.LENGTH_SHORT).show()
            }
        }

        //Setup RecyclerView of folder options
        setupFoldersRecyclerView(folderNames, onClickListener, bottomSheetDialog)

        bottomSheetDialog?.show()
    }

    fun changeNovelFolder(novelPosition: Int, toFolderPos: Int) {
        //if the current folder was not selected
        if (args.position != toFolderPos) {
            //append the novel to the Folder user wants to move it to
            folderList[toFolderPos].webNovels.add(folder.webNovels[novelPosition])

            //delete the novel from the original Folder
            folder.webNovels.removeAt(novelPosition)

            //notify adapter that item was removed
            novelsAdapter.notifyItemRemoved(novelPosition)
        }
    }

    private fun getFolderNames(): MutableList<String> {
        val folderNames = mutableListOf<String>()

        folderList.forEach { folderNames.add(it.name) }

        return folderNames
    }

    private fun setupFoldersRecyclerView(folderNames: MutableList<String>,onClickListener: MoveNovelAdapter.OnClickListener,bottomSheetDialog: BottomSheetDialog?) {
        val foldersAdapter = MoveNovelAdapter(folderNames, onClickListener)
        val recyclerView = bottomSheetDialog?.findViewById<RecyclerView>(R.id.rv_folders)

        recyclerView?.adapter = foldersAdapter
        recyclerView?.layoutManager = LinearLayoutManager(context)
    }
}