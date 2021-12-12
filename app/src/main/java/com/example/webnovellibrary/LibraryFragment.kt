package com.example.webnovellibrary

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LibraryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        setHasOptionsMenu(true)

//        view.findViewById<Button>(R.id.button).setOnClickListener {
//            view.findNavController().navigate(R.id.action_libraryFragment_to_novelsFragment)
//        }

        val rclView = view.findViewById<RecyclerView>(R.id.recycler_view)

        val folders: MutableList<String> = getData()

        //todo click listener
        val onClickListener = object: FolderAdapter.OnClickListener {
            override fun onItemClicked(position: Int) {

                Toast.makeText(context, "Clicked ${folders[position]}", Toast.LENGTH_SHORT).show()
            }
        }

        val folderAdapter = FolderAdapter(folders, onClickListener)
        rclView.adapter = folderAdapter

        rclView.layoutManager = LinearLayoutManager(context)


        // Inflate the layout for this fragment
        return view
    }

    //adds items in menu resource file to the toolbar
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
//        menu.clear()
        menuInflater.inflate(R.menu.menu_toolbar_library, menu)
        return super.onCreateOptionsMenu(menu, menuInflater)
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