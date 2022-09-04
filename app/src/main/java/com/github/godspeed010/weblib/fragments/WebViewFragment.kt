package com.github.godspeed010.weblib.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.github.godspeed010.weblib.R
import com.github.godspeed010.weblib.hideKeyboard
import com.github.godspeed010.weblib.preferences.PreferencesUtils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber
import kotlin.properties.Delegates

class WebViewFragment : Fragment() {

    lateinit var mainToolbar: MaterialToolbar
    lateinit var webViewToolbar: Toolbar
    lateinit var bottomNav: BottomNavigationView
    lateinit var navHostFragment: NavHostFragment
    lateinit var lastVisitedUrl: String
    lateinit var mAdView: AdView
    lateinit var webView: WebView

    var novelPosition by Delegates.notNull<Int>()
    var folderPosition by Delegates.notNull<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)

        navHostFragment = (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val url: String = WebViewFragmentArgs.fromBundle(requireArguments()).url
        novelPosition = WebViewFragmentArgs.fromBundle(requireArguments()).novelPosition
        folderPosition = WebViewFragmentArgs.fromBundle(requireArguments()).folderPosition

        //set lastVisitedUrl to original url. Prevents crashing if user returns from WebView before it's loaded
        lastVisitedUrl = url

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
            //called every time URL changes
            override fun doUpdateVisitedHistory(wv: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(wv, url, isReload)

                Timber.d("URL CHANGE to $url")

                if (view != null) {
                    //Update address bar
                    view.findViewById<EditText>(R.id.et_address_bar).setText(url)

                    if (url != null) {
                        lastVisitedUrl = url
                    }
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
        view.findViewById<EditText>(R.id.et_address_bar)
            .setOnEditorActionListener { textView, i, keyEvent ->
                Timber.d("Action in address bar")

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

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_webview, menu)
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

    override fun onStop() {
        super.onStop()

        overwriteSave()
    }

    fun overwriteSave() {
        val folders = PreferencesUtils.loadFolders(activity)

        //update the url for the novel
        folders[folderPosition].webNovels[novelPosition].url = lastVisitedUrl.toString()

        //save the updated data
        PreferencesUtils.saveFolders(activity, folders)
    }
}