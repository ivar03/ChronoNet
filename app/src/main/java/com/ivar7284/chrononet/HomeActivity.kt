package com.ivar7284.chrononet

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.ivar7284.chrononet.dataclasses.HistoryEntry
import com.ivar7284.chrononet.dataclasses.WeatherResponse
import com.ivar7284.chrononet.functions.HistoryDatabase
import com.ivar7284.chrononet.utils.HistoryDao
import com.ivar7284.chrononet.utils.RetrofitInstance
import kotlinx.coroutines.launch
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.BuildConfig
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.GeckoSession.NavigationDelegate
import org.mozilla.geckoview.WebRequestError
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import kotlin.concurrent.timerTask

class HomeActivity : AppCompatActivity() {

    private lateinit var db: HistoryDatabase
    private lateinit var historyDao: HistoryDao

    private lateinit var timeTv: TextView
    private lateinit var amPmTv: TextView
    private lateinit var weather: TextView

    private lateinit var searchUrl: EditText
    private lateinit var homeBtn: ImageView
    private lateinit var optionBtn: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var tabBtn: TextView

    private lateinit var geckoView: GeckoView
    private lateinit var session: GeckoSession

    private var sRuntime: GeckoRuntime? = null

    @SuppressLint("MissingInflatedId")
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

        //database initialization
        db = HistoryDatabase.getDatabase(this)
        historyDao = db.historyDao()

        //home page background setup
        val layout: ImageView = findViewById(R.id.background_image)
        val assetManager = assets
        val inputStream = assetManager.open("backgroundHome.jpg")
        val bitmap = BitmapFactory.decodeStream(inputStream)
        layout.setImageBitmap(bitmap)

        //setup time and weather for home screen
        timeTv = findViewById(R.id.time_tv)
        amPmTv = findViewById(R.id.am_pm_tv)
        weather = findViewById(R.id.weather_tv)

        updateTimeAndWeather()

        Timer().schedule(timerTask {
            runOnUiThread {
                updateTimeAndWeather()
            }
        }, 0, 60000)

        // initializing views
        initializeViews()

        // Setup GeckoView
        setupGeckoView()

        // functions setups
        // todo:select all text when search has focus/ is click upon
        homeBtn.setOnClickListener {
            session.close()
            session = GeckoSession()
            geckoView.visibility = View.GONE

            timeTv.visibility = View.VISIBLE
            amPmTv.visibility = View.VISIBLE
            weather.visibility = View.VISIBLE

            searchUrl.text.clear()

            updateTimeAndWeather()
        }

        optionBtn.setOnClickListener { view ->
            popupMenu(view)
        }


    }

    // custom popup function:
    @SuppressLint("MissingInflatedId")
    private fun popupMenu(view: View) {
        // Create a custom layout for the popup menu (Inflate only once)
        val popupLayout = LayoutInflater.from(this).inflate(R.layout.custom_popup_layout, null)

        // Create a PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupLayout,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            //elevation = 5f
            //setBackgroundDrawable(null)
        }

        // Set up the horizontal items (forward, backward, star, reload)
        val horizontalLayout: LinearLayout = popupLayout.findViewById(R.id.horizontal_layout)
        horizontalLayout.removeAllViews() // Clear any previous views

        // Create and configure forward icon
        val forwardIcon = ImageView(this).apply {
            setImageResource(R.drawable.forward_icon)
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.icon_size),
                resources.getDimensionPixelSize(R.dimen.icon_size)
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.icon_margin)
                marginEnd = resources.getDimensionPixelSize(R.dimen.icon_margin)
            }
        }

        // Create and configure backward icon
        val backwardIcon = ImageView(this).apply {
            setImageResource(R.drawable.backward_icon)
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.icon_size),
                resources.getDimensionPixelSize(R.dimen.icon_size)
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.icon_margin)
                marginEnd = resources.getDimensionPixelSize(R.dimen.icon_margin)
            }
        }

        // Create and configure star icon
        val starIcon = ImageView(this).apply {
            setImageResource(R.drawable.star_icon)
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.icon_size),
                resources.getDimensionPixelSize(R.dimen.icon_size)
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.icon_margin)
                marginEnd = resources.getDimensionPixelSize(R.dimen.icon_margin)
            }
        }

        // Create and configure reload icon
        val reloadIcon = ImageView(this).apply {
            setImageResource(R.drawable.reload_icon)
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.icon_size),
                resources.getDimensionPixelSize(R.dimen.icon_size)
            ).apply {
                marginStart = resources.getDimensionPixelSize(R.dimen.icon_margin)
                marginEnd = resources.getDimensionPixelSize(R.dimen.icon_margin)
            }
        }

        // Add the icons to the horizontal layout
        horizontalLayout.apply {
            addView(forwardIcon)
            addView(backwardIcon)
            addView(starIcon)
            addView(reloadIcon)
        }

        // Set click listeners for each icon
        forwardIcon.setOnClickListener {
            session.goForward()
            popupWindow.dismiss()
        }
        backwardIcon.setOnClickListener {
            session.goBack()
            popupWindow.dismiss()
        }
        starIcon.setOnClickListener {
            // Bookmark action (e.g., add current page to bookmarks)
            //todo:addBookmark(session.location)
            popupWindow.dismiss()
        }
        reloadIcon.setOnClickListener {
            session.reload()
            popupWindow.dismiss()
        }

        // Set up the vertical items (History, Bookmarks, etc.)
        val verticalLayout: LinearLayout = popupLayout.findViewById(R.id.vertical_layout)
        verticalLayout.removeAllViews() // Clear any previous views

        // Create menu items with improved styling
        val menuItems = listOf(
            "History",
            "Bookmarks",
            "Delete Browsing History",
            "Settings",
            "Incognito Mode",
            "New Tab"
        ).map { text ->
            createMenuItem(text).apply {
                setBackgroundResource(android.R.drawable.list_selector_background)
                setPadding(
                    resources.getDimensionPixelSize(R.dimen.menu_item_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.menu_item_padding_vertical),
                    resources.getDimensionPixelSize(R.dimen.menu_item_padding_horizontal),
                    resources.getDimensionPixelSize(R.dimen.menu_item_padding_vertical)
                )
            }
        }

        // Add menu items to vertical layout
        menuItems.forEach { verticalLayout.addView(it) }

        // Set click listeners for menu items
        menuItems[0].setOnClickListener {
            //todo:openHistory()
            startActivity(Intent(applicationContext, HistoryActivity::class.java))
            popupWindow.dismiss()
        }
        menuItems[1].setOnClickListener {
            //todo:openBookmarks()
            popupWindow.dismiss()
        }
        menuItems[2].setOnClickListener {
            //todo: left
            popupWindow.dismiss()
        }
        menuItems[3].setOnClickListener {
            //todo:openSettings()
            popupWindow.dismiss()
        }
        menuItems[4].setOnClickListener {
            //todo:toggleIncognitoMode()
            popupWindow.dismiss()
        }
        menuItems[5].setOnClickListener {
            //todo:openNewTab()
            popupWindow.dismiss()
        }

        // Measure the popup layout
        popupLayout.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // Calculate the position to show the popup above the button
        val location = IntArray(2)
        view.getLocationInWindow(location)

        // Calculate Y position (subtract popup height and add some padding)
        val yOffset = location[1] - popupLayout.measuredHeight -
                resources.getDimensionPixelSize(R.dimen.popup_vertical_offset)-11

        // Show the popup window above the view
        popupWindow.showAtLocation(
            view,
            android.view.Gravity.TOP or android.view.Gravity.END,
            8,
            yOffset
        )
    }

    // Helper function to create a menu item TextView with improved styling
    private fun createMenuItem(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.white, theme))
        }
    }

    private fun initializeViews() {
        searchUrl = findViewById(R.id.search_et)
        homeBtn = findViewById(R.id.home_btn)
        optionBtn = findViewById(R.id.options_btn)
        progressBar = findViewById(R.id.progressBar)
        tabBtn = findViewById(R.id.tab_btn)
    }

    private fun setupGeckoView() {
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
        session.setNavigationDelegate(object : NavigationDelegate {
            override fun onLocationChange(session: GeckoSession, url: String?) {
                if (url != "about:blank")
                    searchUrl.setText(url)
                    saveHistory(url.toString())
            }
        })


        if (sRuntime == null) sRuntime = GeckoRuntime.create(this)

        session.open(sRuntime!!)
        geckoView.setSession(session)

        // Use imeOptions to handle "Enter" or "Search" button on the keyboard
        searchUrl.setImeOptions(EditorInfo.IME_ACTION_SEARCH)
        searchUrl.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                // Close keyboard automatically
                closeKeyboard()

                // Handle search action
                val url = searchUrl.text.trim().toString()

                if (url.isNotEmpty()) {
                    session.close()
                    session = GeckoSession()
                    setupGeckoView()

                    geckoView.visibility = View.VISIBLE
                    timeTv.visibility = View.GONE
                    amPmTv.visibility = View.GONE
                    weather.visibility = View.GONE
                    session.loadUri(url)
                    searchUrl.clearFocus()
                }
                true // Indicate that the action was handled
            } else {
                false
            }
        }
    }

    private fun saveHistory(url: String) {
        lifecycleScope.launch {
            val historyEntry = HistoryEntry(url = url)
            historyDao.insertHistory(historyEntry)
        }
    }

    private fun updateTimeAndWeather() {
        // Get the current time in 12-hour format
        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        val time = timeFormat.format(Date())

        // Determine AM or PM
        val amPmFormat = SimpleDateFormat("a", Locale.getDefault())
        val amPm = amPmFormat.format(Date())

        timeTv.text = time
        amPmTv.text = amPm

        // fetch weather
        fetchWeather()
    }

    private fun fetchWeather() {
        val apiKey = com.ivar7284.chrononet.BuildConfig.WEATHER_API_KEY
        val city = "Delhi"

        // Make the API call
        RetrofitInstance.weatherApiService.getWeather(city, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    if (weatherResponse != null) {
                        val temperature = weatherResponse.main.temp
                        val description = weatherResponse.weather[0].description
                        val weatherText = "Weather: $temperature°C, $description"
                        weather.text = weatherText
                    }
                } else {
                    Toast.makeText(applicationContext, "Error fetching weather", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(applicationContext, "Failed to connect to the weather service", Toast.LENGTH_SHORT).show()
            }
        })
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
