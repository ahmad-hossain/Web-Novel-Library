package com.example.webnovellibrary

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar


class WebViewFragment : Fragment() {

    private val TAG = "WebViewFragment"

    lateinit var mainToolbar: MaterialToolbar
    lateinit var webViewToolbar: Toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)

        val url: String = WebViewFragmentArgs.fromBundle(requireArguments()).url
        val position: Int = WebViewFragmentArgs.fromBundle(requireArguments()).position

//        setHasOptionsMenu(true)

        //hide the main toolbar
        mainToolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
        mainToolbar.visibility = View.GONE

        //set custom toolbar from xml
        webViewToolbar = view.findViewById<Toolbar>(R.id.webview_toolbar)
        (activity as AppCompatActivity).setSupportActionBar(webViewToolbar)

        //setup toolbar with nav to enable using UP button
        val navHostFragment = (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val builder = AppBarConfiguration.Builder(navController.graph)
        val appBarConfiguration = builder.build()
        webViewToolbar.setupWithNavController(navController, appBarConfiguration)


        val webView = view.findViewById<WebView>(R.id.webview)

        webView.settings.javaScriptEnabled = true

        webView.loadUrl(url)

        //scroll to top of webpage
        webView.scrollTo(0,0)

        val mWebViewClient: WebViewClient = object : WebViewClient() {
            //called every time URL changes
            override fun doUpdateVisitedHistory(wv: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(wv, url, isReload)

                Log.d(TAG, "URL CHANGE to $url")

                if (view != null) {
                    //Update address bar
                    view.findViewById<EditText>(R.id.et_address_bar).setText(url)

                    //Save the novel position and the last url to send back to NovelsFragment

                    navController.previousBackStackEntry?.savedStateHandle?.set("key", listOf(url, "$position"))
                }

            }
        }

        webView.webViewClient = mWebViewClient

        //enable using back button to go to prev. webpage. Returns to prev. Frag. if can't go back anymore
        webView.setOnKeyListener { view, i, keyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                webView.goBack() // Navigate back to previous web page if there is one
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        //switch back to main toolbar by making it visible again
        mainToolbar.visibility = View.VISIBLE
        (activity as AppCompatActivity).setSupportActionBar(mainToolbar)
    }

}