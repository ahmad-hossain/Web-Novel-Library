package com.example.webnovellibrary

import android.R.attr
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import android.R.attr.label
import android.content.*
import android.icu.text.CaseMap
import android.preference.PreferenceManager

import androidx.core.content.ContextCompat

import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.fragment.navArgs
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class NovelsFragment : Fragment() {

    private val args by navArgs<NovelsFragmentArgs>()

    private val TAG = "NovelsFragment"
    lateinit var webNovelsList: MutableList<WebNovel>
    lateinit var novelsAdapter: NovelsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_novels, container, false)

        setHasOptionsMenu(true)

        val folder = args.folder

//        val folder = NovelsFragmentArgs.fromBundle(requireArguments()).folder
        webNovelsList = folder.webNovels

        //hardcoded for testing
//        webNovelsList = getData()

        Log.d(TAG, "opened folder with name: ${folder.name} and ${webNovelsList.size} novels")

        // populate RecyclerView
        val rclView = view.findViewById<RecyclerView>(R.id.recycler_view2)

        //click listener for RecyclerView items
        val onClickListener = object: NovelsAdapter.OnClickListener {
            override fun onItemClicked(position: Int) {

                Toast.makeText(context, "Clicked ${webNovelsList[position].title}", Toast.LENGTH_SHORT).show()

            }

            override fun onCopyClicked(position: Int) {
                Log.d(TAG, "onCopyClicked: clicked copy for ${webNovelsList[position].title}")

                val clipboard: ClipboardManager? =
                    context?.let { getSystemService(it, ClipboardManager::class.java) }
                val clip = ClipData.newPlainText(label.toString(), webNovelsList[position].url)
                clipboard?.setPrimaryClip(clip)

                Toast.makeText(context, "Copied URL!", Toast.LENGTH_SHORT).show()
            }

            override fun onEditClicked(position: Int) {
                Log.d(TAG, "onEditClicked: clicked edit for ${webNovelsList[position].title}")

                val builder = AlertDialog.Builder(context)
                builder.setTitle("Edit Web Novel")

                val viewInflated: View = LayoutInflater.from(context)
                    .inflate(R.layout.popup_web_novel, view as ViewGroup?, false)

                // Set up the input
                val webNovelTitle = viewInflated.findViewById(R.id.et_webNovel_name) as TextInputEditText
                val webNovelUrl = viewInflated.findViewById(R.id.et_webNovel_url) as TextInputEditText

                webNovelTitle.setText(webNovelsList[position].title)
                webNovelUrl.setText(webNovelsList[position].url)

                // Specify the type of input expected
                builder.setView(viewInflated)

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
        }

        //Setup and display the RecyclerView
        novelsAdapter = NovelsAdapter(webNovelsList, onClickListener)
        rclView.adapter = novelsAdapter
        rclView.layoutManager = LinearLayoutManager(context)

        return view
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
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Add Web Novel")

                val viewInflated: View = LayoutInflater.from(context)
                    .inflate(R.layout.popup_web_novel, view as ViewGroup?, false)

                // Set up the input
                val webNovelTitle = viewInflated.findViewById(R.id.et_webNovel_name) as TextInputEditText
                val webNovelUrl = viewInflated.findViewById(R.id.et_webNovel_url) as TextInputEditText

                // Specify the type of input expected
                builder.setView(viewInflated)

                builder.setPositiveButton(android.R.string.ok,
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()

                        val title = webNovelTitle.text.toString()
                        val url = webNovelUrl.text.toString()

                        Log.d(TAG, "new web novel requested: $title")

                        addNovel(title, url)

                    })

                builder.setNegativeButton(android.R.string.cancel,
                    DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                builder.show()

                return true
            }
        }

        return false
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
        val oldFolders = loadData()

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
}