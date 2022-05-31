package com.github.godspeed010.weblib.fragments

import android.content.Context
import android.content.SharedPreferences
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
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.properties.Delegates
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import com.github.godspeed010.weblib.R
import com.github.godspeed010.weblib.hideKeyboard
import com.github.godspeed010.weblib.preferences.PreferencesUtils
import java.util.*


class WebViewFragment : Fragment() {

    private val TAG = "WebViewFragment"

    lateinit var mainToolbar: MaterialToolbar
    lateinit var webViewToolbar: Toolbar
    lateinit var bottomNav: BottomNavigationView
    lateinit var navHostFragment: NavHostFragment
    lateinit var mAdView: AdView
    lateinit var webView: WebView
    lateinit var lastVisitedUrl: String
    lateinit var timer: Timer

    var lastScroll by Delegates.notNull<Int>()
    var novelPosition by Delegates.notNull<Int>()
    var folderPosition by Delegates.notNull<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)

        navHostFragment = (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val url: String = WebViewFragmentArgs.fromBundle(requireArguments()).url
        novelPosition = WebViewFragmentArgs.fromBundle(requireArguments()).novelPosition
        folderPosition = WebViewFragmentArgs.fromBundle(requireArguments()).folderPosition

        lastVisitedUrl = url
        lastScroll = WebViewFragmentArgs.fromBundle(requireArguments()).scrollY

        setHasOptionsMenu(true)

        setToolbarAndNavView(View.GONE)

        //set custom toolbar from xml
        webViewToolbar = view.findViewById(R.id.webview_toolbar)
        (activity as AppCompatActivity).setSupportActionBar(webViewToolbar)

        //setup toolbar with nav to enable using UP button
        setupToolbarWithNav(webViewToolbar)


        webView = view.findViewById(R.id.webview)

        webView.settings.javaScriptEnabled = true

        webView.loadUrl(url)

        val mWebViewClient: WebViewClient = object : WebViewClient() {
            private var pageError = false
            //called every time URL changes
            override fun doUpdateVisitedHistory(wv: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(wv, url, isReload)

                Log.d(TAG, "URL CHANGE to $url")

                if (view != null) {
                    //Update address bar
                    view.findViewById<EditText>(R.id.et_address_bar).setText(url)

                    if (url != null) {
                        lastScroll = 0
                        lastVisitedUrl = url
                    }
                }
            }

            override fun onReceivedHttpError(wv: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(wv, request, errorResponse)

                pageError = true
            }

            override fun onPageFinished(wv: WebView?, url: String?) {
                super.onPageFinished(wv, url)

                if (url == WebViewFragmentArgs.fromBundle(requireArguments()).url && !pageError) {
                    wv!!.scrollTo(0, WebViewFragmentArgs.fromBundle(requireArguments()).scrollY)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setOnScrollChangeListener { view, _, _, _, _ ->
                lastScroll = view.scrollY
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
        view.findViewById<EditText>(R.id.et_address_bar)
            .setOnEditorActionListener { textView, i, keyEvent ->
                Log.d(TAG, "Action in address bar")

                hideKeyboard()

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

        MobileAds.initialize(activity)

        mAdView = view.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        timer = Timer("AutoSave")
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                overwriteSave(lastVisitedUrl, lastScroll)
            }
        }, 30000, 30000)

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_webview, menu)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && resources.configuration.isNightModeActive) ||
                (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) == Configuration.UI_MODE_NIGHT_YES
            ) {
                menu.findItem(R.id.dark_mode).isChecked = true
                WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        } else {
            menu.findItem(R.id.dark_mode).isVisible = false
        }
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh -> webView.reload()
            R.id.share -> shareUrl(webView.url)
            R.id.dark_mode -> toggleDarkMode(item)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()

        timer.cancel()
    }

    private fun shareUrl(url: String?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(Intent.createChooser(shareIntent, "Share This Website!"))
    }

    private fun toggleDarkMode(item: MenuItem) {
        if (item.isChecked) {
            //uncheck the checkbox
            item.isChecked = false

            //turn OFF WebView dark mode
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_OFF)
            }
        } else {
            //check the checkbox
            item.isChecked = true

            //turn ON WebView dark mode
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        //switch back to main toolbar by making it visible again
        setToolbarAndNavView(View.VISIBLE)
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

    fun setToolbarAndNavView(visibility: Int) {
        //hide the main toolbar
        mainToolbar = (activity as AppCompatActivity).findViewById(R.id.toolbar)
        mainToolbar.visibility = visibility

        //hide bottom nav.
        bottomNav = (activity as AppCompatActivity).findViewById(R.id.bottom_nav)
        bottomNav.visibility = visibility
    }

    fun overwriteSave(url: String, scrollY: Int) {
        val folders = PreferencesUtils.loadFolders(activity)

        //update the url for the novel
        folders[folderPosition].webNovels[novelPosition].url = url
        folders[folderPosition].webNovels[novelPosition].scroll = scrollY

        //save the updated data
        PreferencesUtils.saveFolders(activity, folders)
    }
}