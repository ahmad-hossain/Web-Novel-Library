package com.github.godspeed010.weblib.fragments

import android.content.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat.getSystemService
import com.github.godspeed010.weblib.R
import com.github.godspeed010.weblib.models.WebNovel
import com.github.godspeed010.weblib.adapters.NovelsAdapter
import com.github.godspeed010.weblib.focusAndShowKeyboard
import com.github.godspeed010.weblib.hideKeyboard
import com.github.godspeed010.weblib.preferences.PreferencesUtils


class SearchFragment : Fragment() {

    private val TAG = "SearchFragment"

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NovelsAdapter
    private lateinit var onClickListener: NovelsAdapter.OnClickListener

    private var filteredList = mutableListOf<WebNovel>()
    private var filteredListIndices = mutableListOf<MutableList<Int>>()    //holds folder and novel indices for each item in filteredList

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val searchEditText = view.findViewById<EditText>(R.id.et_search)

        //focus on and open keyboard for EditText as soon as Fragment is opened
        searchEditText.focusAndShowKeyboard()
        searchEditText.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filter( searchEditText.text.toString() )

            }

            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        recyclerView = view.findViewById(R.id.recycler_view_search)

        //copied from NovelsAdapter
        onClickListener = object: NovelsAdapter.OnClickListener {
            override fun onItemClicked(position: Int) {
                //send url, folder position, and novel position to WebViewFragment so it can save on its own
                val action = SearchFragmentDirections
                    .actionSearchFragmentToWebViewFragment(
                        novel = filteredList[position],
                        folderPosition = filteredListIndices[position][0],
                        novelPosition = filteredListIndices[position][1])
                view.findNavController().navigate(action)
            }

            override fun onCopyClicked(position: Int) {
                Log.d(TAG, "onCopyClicked: clicked copy for index $position")

                val clipboard: ClipboardManager? =
                    context?.let {
                        getSystemService(
                            it,
                            ClipboardManager::class.java
                        )
                    }
                val clip = ClipData.newPlainText(android.R.attr.label.toString(), filteredList[position].url)
                clipboard?.setPrimaryClip(clip)

                Toast.makeText(context, "Copied URL!", Toast.LENGTH_SHORT).show()
            }

            override fun onMoreClicked(position: Int) {
            }
        }

        setupRecyclerView()

        return view
    }

    override fun onResume() {
        super.onResume()

        //set toolbar title
        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.search)
    }

    private fun setupRecyclerView() {
        //create adapter with empty list. Change cardLayout type so no more Button is shown
        adapter = NovelsAdapter(mutableListOf(), onClickListener, cardLayoutType = 1)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun filter(text: String) {
        //clear data from previous search
        filteredList.clear()
        filteredListIndices.clear()

        val folders = PreferencesUtils.loadFolders(activity)

        for (folderIndex in folders.indices) {
            val folder = folders[folderIndex]

            for (novelIndex in folder.webNovels.indices) {
                val novel = folder.webNovels[novelIndex]

                //if search query is found in a novel's title or URL
                if (novel.title.lowercase().contains(text.lowercase()) || novel.url.lowercase().contains(text.lowercase())) {
                    filteredList.add(novel)
                    filteredListIndices.add(mutableListOf(folderIndex, novelIndex))
                }
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(context, "No Data Found..", Toast.LENGTH_SHORT).show()
        }

        //make adapter show the filtered list
        adapter.filterList(filteredList)
    }

    override fun onStop() {
        super.onStop()

        hideKeyboard()
    }
}

