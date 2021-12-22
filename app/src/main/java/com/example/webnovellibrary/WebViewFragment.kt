package com.example.webnovellibrary

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar


class WebViewFragment : Fragment() {

    private val TAG = "WebViewFragment"

    lateinit var mainToolbar: MaterialToolbar
    lateinit var webViewToolbar: Toolbar

    lateinit var navHostFragment: NavHostFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)

        navHostFragment = (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val url: String = WebViewFragmentArgs.fromBundle(requireArguments()).url
        val position: Int = WebViewFragmentArgs.fromBundle(requireArguments()).position

//        setHasOptionsMenu(true)

        //hide the main toolbar
        mainToolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
        mainToolbar.visibility = View.GONE

        //set custom toolbar from xml
        webViewToolbar = view.findViewById(R.id.webview_toolbar)
        (activity as AppCompatActivity).setSupportActionBar(webViewToolbar)

        //setup toolbar with nav to enable using UP button
        setupToolbarWithNav(webViewToolbar)


        val webView = view.findViewById<WebView>(R.id.webview)

        webView.settings.javaScriptEnabled = true

        webView.loadUrl(url)

        val mWebViewClient: WebViewClient = object : WebViewClient() {
            //called every time URL changes
            override fun doUpdateVisitedHistory(wv: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(wv, url, isReload)

                Log.d(TAG, "URL CHANGE to $url")

                if (view != null) {
                    //Update address bar
                    view.findViewById<EditText>(R.id.et_address_bar).setText(url)

                    //Save the novel position and the last url to send back to NovelsFragment
                    navHostFragment.navController.previousBackStackEntry?.savedStateHandle?.set("key", listOf(url, "$position"))
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

        //address bar action listener for letting user change url
        view.findViewById<EditText>(R.id.et_address_bar).setOnEditorActionListener { textView, i, keyEvent ->
            Log.d(TAG, "Action in address bar")

            closeKeyboard(view)

            val address = textView.text.toString()
            val isAddress = Patterns.WEB_URL.matcher(address).matches()

            //load webpage if valid
            if (isAddress) {
                webView.loadUrl(address)
            }
            //Google search if not valid webpage
            else {
                webView.loadUrl(
                    "https://www.google.com/search?q=$address"
                )
            }

            true
        }

        return view
    }

    private fun closeKeyboard(view: View) {
        val manager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        manager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        //switch back to main toolbar by making it visible again
        mainToolbar.visibility = View.VISIBLE
        (activity as AppCompatActivity).setSupportActionBar(mainToolbar)

        //setup toolbar with nav to enable using UP button
        setupToolbarWithNav(mainToolbar)
    }

    fun setupToolbarWithNav(toolbar: Toolbar) {
        //setup toolbar with nav to enable using UP button
        val builder = AppBarConfiguration.Builder(navHostFragment.navController.graph)
        val appBarConfiguration = builder.build()
        toolbar.setupWithNavController(navHostFragment.navController, appBarConfiguration)
    }

}