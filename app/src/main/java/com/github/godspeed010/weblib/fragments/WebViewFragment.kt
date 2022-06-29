package com.github.godspeed010.weblib.fragments

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.*
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
import android.widget.Toast
import com.github.godspeed010.weblib.R
import com.github.godspeed010.weblib.hideKeyboard
import com.github.godspeed010.weblib.models.WebNovel
import com.github.godspeed010.weblib.preferences.PreferencesUtils
import java.text.NumberFormat
import java.util.*

class WebViewFragment : Fragment() {

    private val TAG = "WebViewFragment"
    private var timer: Timer? = null

    lateinit var mainToolbar: MaterialToolbar
    lateinit var webViewToolbar: Toolbar
    lateinit var bottomNav: BottomNavigationView
    lateinit var navHostFragment: NavHostFragment
    lateinit var mAdView: AdView
    lateinit var webView: WebView
    lateinit var lastVisitedUrl: String
    lateinit var lastPageTitle: String

    var lastProgression by Delegates.notNull<Float>()
    var novelPosition by Delegates.notNull<Int>()
    var folderPosition by Delegates.notNull<Int>()
    var pageError by Delegates.notNull<Boolean>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)

        navHostFragment = (activity as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val novel: WebNovel = WebViewFragmentArgs.fromBundle(requireArguments()).novel
        novelPosition = WebViewFragmentArgs.fromBundle(requireArguments()).novelPosition
        folderPosition = WebViewFragmentArgs.fromBundle(requireArguments()).folderPosition

        // set lastVisitedUrl and lastScroll to their original values.
        // Prevents crashing if user returns from WebView before it's loaded
        lastVisitedUrl = novel.url
        lastProgression = novel.progression

        Log.d(TAG, "density=${resources.displayMetrics.density}, densityDpi=${resources.displayMetrics.densityDpi}")

        setHasOptionsMenu(true)

        setToolbarAndNavView(View.GONE)

        //set custom toolbar from xml
        webViewToolbar = view.findViewById(R.id.webview_toolbar)
        (activity as AppCompatActivity).setSupportActionBar(webViewToolbar)

        //setup toolbar with nav to enable using UP button
        setupToolbarWithNav(webViewToolbar)


        webView = view.findViewById(R.id.webview)

        webView.settings.javaScriptEnabled = true

        webView.loadUrl(novel.url)

        val mWebViewClient: WebViewClient = object : WebViewClient() {
            //called every time URL changes
            override fun doUpdateVisitedHistory(wv: WebView?, url: String?, isReload: Boolean) {
                super.doUpdateVisitedHistory(wv, url, isReload)

                Log.d(TAG, "URL CHANGE to $url")

                if (view != null) {
                    //Update address bar
                    view.findViewById<EditText>(R.id.et_address_bar).setText(url)

                    if (url != null) {
                        pageError = false
                        lastProgression = 0f
                        lastVisitedUrl = url
                    }
                }
            }

            override fun onReceivedHttpError(wv: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(wv, request, errorResponse)

                if (request?.url.toString() == wv?.url) pageError = true
            }

            override fun onPageFinished(wv: WebView?, loadedUrl: String?) {
                super.onPageFinished(wv, loadedUrl)

                if ((loadedUrl == novel.url) && !pageError) {
                    if (novel.progression != 0f) {
                        val toast: Toast = Toast.makeText(context, "Please wait for page to scroll.", Toast.LENGTH_LONG)
                        toast.show()
                        wv?.postDelayed({
                            val scrollY: Int = calculateScrollYFromProgression(novel.progression, wv)
                            val progressionPct: String = NumberFormat.getPercentInstance().let {
                                it.minimumFractionDigits = 1
                                it.format(novel.progression)
                            }
                            Log.d(
                                TAG,
                                "Page finished loading, scrolling to $scrollY ($progressionPct)"
                            )
                            wv.scrollTo(0, scrollY)
                            toast.cancel()
                        }, 2000)
                    }
                }

                lastPageTitle = wv?.title ?: loadedUrl!!
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setOnScrollChangeListener { view, _, _, _, _ ->
                if (!pageError) {
                    lastProgression = calculateProgression(webView)
                    val progressionPct: String = NumberFormat.getPercentInstance().let {
                        it.minimumFractionDigits = 1
                        it.format(lastProgression)
                    }
                    Log.v(
                        TAG,
                        "Page progression is now $progressionPct"
                    )
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

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_toolbar_webview, menu)
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            // Automatically turn on dark mode if night mode on the device is active.
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && resources.configuration.isNightModeActive) ||
                (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) == Configuration.UI_MODE_NIGHT_YES
            ) {
                menu.findItem(R.id.dark_mode).isChecked = true
                WebSettingsCompat.setForceDark(webView.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
        } else { // If forcing dark mode is not supported by the web view, hide the option to enable dark mode.
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

    private fun shareUrl(url: String?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(Intent.createChooser(shareIntent, "Share This Website!"))
    }

    override fun onPause() {
        super.onPause()

        overwriteSave()

        stopTimer()
    }

    override fun onResume() {
        super.onResume()

        startTimer()
    }

    private fun startTimer() {
        if (timer != null) return
        timer = Timer("AutoSave")
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                overwriteSave()
            }
        }, 30000, 30000)
    }

    private fun stopTimer() {
        if (timer == null) return
        timer!!.cancel()
        timer = null
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

    fun overwriteSave() {
        val folders = PreferencesUtils.loadFolders(activity)

        //update the url for the novel
        folders[folderPosition].webNovels[novelPosition].url = lastVisitedUrl
        folders[folderPosition].webNovels[novelPosition].progression = lastProgression

        //save the updated data
        PreferencesUtils.saveFolders(activity, folders)
        Log.d(TAG, "Web Novel saved!")
    }

    private fun calculateProgression(wv: WebView): Float {
        // The 200 subtracted is to bring a little bit above where a user stopped so they can determine where they were again.
        return ((wv.scrollY - wv.top - 200).toFloat() / resources.displayMetrics.density) / wv.contentHeight
    }

    private fun calculateScrollYFromProgression(progression: Float, wv: WebView): Int {
        return ((progression * wv.contentHeight) * resources.displayMetrics.density).toInt() + wv.top
    }
}