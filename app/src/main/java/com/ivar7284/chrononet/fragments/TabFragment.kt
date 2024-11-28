package com.ivar7284.chrononet.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.ivar7284.chrononet.R
import com.ivar7284.chrononet.dataclasses.HistoryEntry
import com.ivar7284.chrononet.functions.HistoryDatabase
import com.ivar7284.chrononet.utils.GeckoRuntimeSingleton
import com.ivar7284.chrononet.utils.HistoryDao
import com.ivar7284.chrononet.utils.UrlUpdateListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.NavigationDelegate
import org.mozilla.geckoview.GeckoView

class TabFragment : Fragment(), UrlUpdateListener {

    private lateinit var db: HistoryDatabase
    private lateinit var historyDao: HistoryDao
    private lateinit var progressBar: ProgressBar

    private lateinit var geckoView: GeckoView
    private lateinit var session: GeckoSession
    private var geckoRuntime: GeckoRuntime? = null

    private var urlListener: UrlUpdateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and DAO
        db = HistoryDatabase.getDatabase(requireContext())
        historyDao = db.historyDao()

        // Initialize session
        session = GeckoSession()

        geckoRuntime = GeckoRuntimeSingleton.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tab, container, false)

        // check if session is previously initialized
        if(!::session.isInitialized){
            session = GeckoSession()
        }

        // get the url from arguments
        val URL = arguments?.getString("URL") ?: "about:blank"
        Log.d("url_home_activity", URL)

        // initialize views
        geckoView = view.findViewById(R.id.geckoview)
        progressBar = view.findViewById(R.id.progressBar)

        session = GeckoSession()
        setupGeckoView(URL)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // check if the host activity implements the interface
        if(context is UrlUpdateListener){
            urlListener = context
        } else {
            throw RuntimeException("$context must implement UrlUpdateListener")
        }

        //ensures that the session is initialized
        if (!::session.isInitialized) {
            session = GeckoSession()
        }

        // session url updates
        setupNavigationDelegate()
    }

    private fun setupNavigationDelegate() {
        session.setNavigationDelegate(object : NavigationDelegate {
            override fun onLocationChange(session: GeckoSession, url: String?) {
                url?.let {
                    if (it != "about:blank") {
                        // Call the interface method to update URL
                        urlListener?.onUrlChanged(it)
                        saveHistory(it)

                        // fetch title
                        session.setContentDelegate(object: GeckoSession.ContentDelegate {
                            override fun onTitleChange(session: GeckoSession, title: String?) {
                                // TODO: Implement title handling
                            }
                        })
                    }
                }
            }
        })
    }

    // setup - geckoview
    private fun setupGeckoView(URL: String) {
        session?.let { session ->
            session.loadUri(URL.trim())
            session.setContentDelegate(object: GeckoSession.ContentDelegate{})
            session.setProgressDelegate(object: GeckoSession.ProgressDelegate {
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

            // Use the singleton runtime
            geckoRuntime?.let { runtime ->
                session.open(runtime)
                geckoView.setSession(session)
            }
        }
    }

    private fun saveHistory(url: String) {
        lifecycleScope.launch {
            val historyEntry = HistoryEntry(url = url)
            historyDao.insertHistory(historyEntry)
        }
    }

    override fun onUrlChanged(url: String) {
        //method defined at home activity
    }

    override fun onDetach() {
        super.onDetach()
        urlListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup resources
        closeSession()
    }

    fun closeSession() {
        try {
            if (::session.isInitialized) {
                session.close()
            }
        } catch (e: Exception) {
            Log.e("TabFragment", "Error closing session", e)
        } finally {
            // Reset session if needed
            if (::session.isInitialized) {
                session = GeckoSession()
            }
        }
    }
}