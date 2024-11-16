package com.ivar7284.chrononet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.GeckoSession.NavigationDelegate
import org.mozilla.geckoview.WebRequestError

class HomeActivity : AppCompatActivity() {

    private lateinit var searchUrl: EditText
    private lateinit var homeBtn: ImageView
    private lateinit var optionBtn: ImageView
    private lateinit var progressBar: ProgressBar

    private lateinit var geckoView: GeckoView
    private lateinit var session: GeckoSession

    private var sRuntime: GeckoRuntime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        //hide status bar
        //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        //nav and status bar color
        window.statusBarColor = resources.getColor(R.color.light_gray,null)
        window.navigationBarColor = resources.getColor(R.color.light_gray,null)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initializing views
        searchUrl = findViewById(R.id.search_et)
        homeBtn = findViewById(R.id.home_btn)
        optionBtn = findViewById(R.id.options_btn)
        progressBar = findViewById(R.id.progressBar)  // Initialize ProgressBar

        // Setup GeckoView
        geckoView = findViewById(R.id.geckoview)
        session = GeckoSession()

        session.setContentDelegate(object : GeckoSession.ContentDelegate {})
        session.setProgressDelegate(object: GeckoSession.ProgressDelegate{
            override fun onProgressChange(session: GeckoSession, progress: Int) {
                progressBar.progress = progress
                if (progress == 100) progressBar.visibility = ProgressBar.GONE
            }

            override fun onPageStart(session: GeckoSession, url: String) {
                progressBar.visibility = ProgressBar.VISIBLE
                progressBar.progress = 0
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                progressBar.visibility = ProgressBar.GONE
            }
        })

        if (sRuntime == null) sRuntime = GeckoRuntime.create(this)

        session.open(sRuntime!!)
        geckoView.setSession(session)

        session.loadUri("about:blank") // Initially load blank page

        // Use imeOptions to handle "Enter" or "Search" button on the keyboard
        searchUrl.setImeOptions(EditorInfo.IME_ACTION_SEARCH)

        searchUrl.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Close the keyboard programmatically
                closeKeyboard()

                // Handle search action
                val url = searchUrl.text.trim().toString()

                if (url.isNotEmpty()) {
                    session.loadUri(url)
                }
                true // Indicate that the action was handled
            } else {
                false
            }
        }
    }

    private fun closeKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchUrl.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources when activity is destroyed
        session.close()
    }
}
