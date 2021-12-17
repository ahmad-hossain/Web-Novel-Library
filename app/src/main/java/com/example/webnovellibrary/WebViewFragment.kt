package com.example.webnovellibrary

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class WebViewFragment : Fragment() {

    lateinit var webView: WebView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)

        setHasOptionsMenu(true)

        val url: String = WebViewFragmentArgs.fromBundle(requireArguments()).url

        webView = view.findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl("https://www.google.com")
        webView.webViewClient = WebViewClient()

        webView.setOnKeyListener { _, _, keyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                webView.goBack()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        return view
    }

    //TODO adds items in menu resource file to the toolbar
//    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
//        menuInflater.inflate(R.menu.menu_toolbar_novels, menu)
//        return super.onCreateOptionsMenu(menu, menuInflater)
//    }

    //TODO does something when a menu item is selected
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.add_webNovel -> {
//                val builder = AlertDialog.Builder(context)
//                builder.setTitle("Add Web Novel")
//
//                val viewInflated: View = LayoutInflater.from(context)
//                    .inflate(R.layout.popup_web_novel, view as ViewGroup?, false)
//
//                // Set up the input
//                val webNovelTitle = viewInflated.findViewById(R.id.et_webNovel_name) as TextInputEditText
//                val webNovelUrl = viewInflated.findViewById(R.id.et_webNovel_url) as TextInputEditText
//
//                // Specify the type of input expected
//                builder.setView(viewInflated)
//
//                builder.setPositiveButton(android.R.string.ok,
//                    DialogInterface.OnClickListener { dialog, which ->
//                        dialog.dismiss()
//
//                        val title = webNovelTitle.text.toString()
//                        val url = webNovelUrl.text.toString()
//
//                        Log.d(TAG, "new web novel requested: $title")
//
//                        addNovel(title, url)
//
//                    })
//
//                builder.setNegativeButton(android.R.string.cancel,
//                    DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
//
//                builder.show()
//
//                return true
//            }
//        }
//
//        return false
//    }
}