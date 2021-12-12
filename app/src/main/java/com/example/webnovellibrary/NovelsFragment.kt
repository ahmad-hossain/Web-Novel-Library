package com.example.webnovellibrary

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

class NovelsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_novels, container, false)

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return view
    }

    //adds items in menu resource file to the toolbar
    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
//        menu.clear()
        menuInflater.inflate(R.menu.menu_toolbar_novels, menu)
        return super.onCreateOptionsMenu(menu, menuInflater)
    }
}