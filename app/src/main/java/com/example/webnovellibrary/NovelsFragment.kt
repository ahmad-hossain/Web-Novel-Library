package com.example.webnovellibrary

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NovelsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_novels, container, false)

        setHasOptionsMenu(true)

//        private val args by navArgs<NovelsFragmentArgs>()     retrieve using args.folder


        val folder = NovelsFragmentArgs.fromBundle(requireArguments()).folder
//        val webNovelsList = folder.webNovels

        //hardcoded for testing
        val webNovelsList = getData()

        Log.d("my_tag", "opened folder with name: ${folder.name} and ${webNovelsList.size} novels")

        //TODO populate RecyclerView
        val rclView = view.findViewById<RecyclerView>(R.id.recycler_view2)

        //click listener for RecyclerView items
        val onClickListener = object: NovelsAdapter.OnClickListener {
            override fun onItemClicked(position: Int) {

                Toast.makeText(context, "Clicked ${webNovelsList[position].title}", Toast.LENGTH_SHORT).show()

            }
        }

        //Setup and display the RecyclerView
        val novelsAdapter = NovelsAdapter(webNovelsList, onClickListener)
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
}