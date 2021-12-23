package com.example.webnovellibrary

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class SearchFragment : Fragment() {

    private val TAG = "SearchFragment"

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NovelsAdapter
    private lateinit var onClickListener: NovelsAdapter.OnClickListener

    private var filteredList = mutableListOf<WebNovel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val searchEditText = view.findViewById<EditText>(R.id.et_search)
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
                Log.d(TAG, "onItemClicked: clicked item $position")

                val action = NovelsFragmentDirections.
                actionNovelsFragmentToWebViewFragment(url = filteredList[position].url, position = position)
                view?.findNavController()?.navigate(action)

            }

            override fun onCopyClicked(position: Int) {
                Log.d(TAG, "onCopyClicked: clicked copy for index $position")

                val clipboard: ClipboardManager? =
                    context?.let {
                        ContextCompat.getSystemService(
                            it,
                            ClipboardManager::class.java
                        )
                    }
                val clip = ClipData.newPlainText(android.R.attr.label.toString(), filteredList[position].url)
                clipboard?.setPrimaryClip(clip)

                Toast.makeText(context, "Copied URL!", Toast.LENGTH_SHORT).show()
            }

            override fun onMoreClicked(position: Int) {
                Log.d(TAG, "onMoreClicked: clicked more for index $position")
                NovelsFragment().showBottomSheetDialog(position)
//                    showBottomSheetDialog(position)
            }
        }

        setupRecyclerView()

        return view
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


        for (folder in loadData()) {
            for (novel in folder.webNovels) {
                //if search query is found in a novel's title or URL
                if (novel.title.lowercase().contains(text.lowercase()) || novel.url.lowercase().contains(text.lowercase())) {
                    filteredList.add(novel)
                }
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(context, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            //make adapter show the filtered list
            adapter.filterList(filteredList)
        }

    }

    fun loadData(): MutableList<Folder> {

        val sharedPreferences: SharedPreferences =
            activity!!.getSharedPreferences("shared preferences", Context.MODE_PRIVATE)

        val gson = Gson()

        val emptyList = Gson().toJson(ArrayList<Folder>())
        val json = sharedPreferences.getString("foldersList", emptyList)

        val type: Type = object : TypeToken<ArrayList<Folder?>?>() {}.type

        var folders: MutableList<Folder> = gson.fromJson(json, type)

        if (folders == null) {

            folders = mutableListOf<Folder>()
        }

        return folders
    }

}

