package com.example.webnovellibrary

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

class NovelsFragment : Fragment() {

    private val TAG = "NovelsFragment"
    lateinit var webNovelsList: MutableList<WebNovel>
    lateinit var novelsAdapter: NovelsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_novels, container, false)

        setHasOptionsMenu(true)

//        private val args by navArgs<NovelsFragmentArgs>()     retrieve using args.folder

        val folder = NovelsFragmentArgs.fromBundle(requireArguments()).folder
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
        }

        //Setup and display the RecyclerView
        novelsAdapter = NovelsAdapter(webNovelsList, onClickListener)
        rclView.adapter = novelsAdapter
        rclView.layoutManager = LinearLayoutManager(context)

        return view
    }

    fun getData(): MutableList<WebNovel> {
        val l = mutableListOf<WebNovel>()
        l.add( WebNovel("Google", "https://google.com") )
        l.add( WebNovel("Bing", "https://bing.com") )
        l.add( WebNovel("Google", "https://google.com") )
        l.add( WebNovel("Bing", "https://bing.com") )
        l.add( WebNovel("Google", "https://google.com") )
        l.add( WebNovel("Bing", "https://bing.com") )
        l.add( WebNovel("Google", "https://google.com") )
        l.add( WebNovel("Bing", "https://bing.com") )
        return l
    }

    //adds items in menu resource file to the toolbar
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
//        menu.clear()
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
}