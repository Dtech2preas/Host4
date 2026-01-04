package com.dtech.browser

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tabManager: TabManager
    private lateinit var etUrl: EditText
    private lateinit var tvAdsBlocked: TextView
    private lateinit var btnDownloadVideo: Button

    private var adsBlockedCount = 0
    private var lastDetectedVideoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val container = findViewById<FrameLayout>(R.id.webview_container)

        // UI Controls
        etUrl = findViewById(R.id.et_url)
        tvAdsBlocked = findViewById(R.id.tv_ads_blocked)
        btnDownloadVideo = findViewById(R.id.btn_download_video)
        val btnBack = findViewById<Button>(R.id.btn_back)
        val btnForward = findViewById<Button>(R.id.btn_forward)
        val btnGo = findViewById<Button>(R.id.btn_go)
        val btnTabs = findViewById<Button>(R.id.btn_tabs)
        val btnDesktopToggle = findViewById<Button>(R.id.btn_desktop_toggle)

        // Initialize TabManager with callbacks
        tabManager = TabManager(
            this,
            container,
            onAdBlocked = {
                runOnUiThread {
                    adsBlockedCount++
                    tvAdsBlocked.text = "Ads Blocked: $adsBlockedCount"
                }
            },
            onVideoDetected = { url ->
                runOnUiThread {
                    lastDetectedVideoUrl = url
                    btnDownloadVideo.visibility = View.VISIBLE
                    // Optional: Highlight button or show a quick toast
                }
            }
        )

        // Setup Buttons
        btnBack.setOnClickListener {
            tabManager.getCurrentTab()?.let {
                if (it.canGoBack()) it.goBack()
            }
        }

        btnForward.setOnClickListener {
            tabManager.getCurrentTab()?.let {
                if (it.canGoForward()) it.goForward()
            }
        }

        btnGo.setOnClickListener {
            loadUrlFromInput()
        }

        btnTabs.setOnClickListener {
            tabManager.createTab("https://www.google.com")
            Toast.makeText(this, "New Tab Created. Total: ${tabManager.getTabCount()}", Toast.LENGTH_SHORT).show()
        }

        btnDesktopToggle.setOnClickListener {
            tabManager.toggleDesktopMode()
        }

        btnDownloadVideo.setOnClickListener {
            val url = lastDetectedVideoUrl
            if (url != null) {
                downloadVideo(url)
            }
        }

        etUrl.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadUrlFromInput()
                true
            } else {
                false
            }
        }

        // Create initial tab
        tabManager.createTab("https://www.google.com")
    }

    private fun loadUrlFromInput() {
        val url = etUrl.text.toString()
        val formattedUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "https://$url"
        }
        // Reset video button on new load? Maybe, but user might want to download previously detected video.
        // For now, let's keep it until new one detected or we could hide it.
        // Let's hide it to avoid confusion if the new page has no video.
        btnDownloadVideo.visibility = View.GONE
        lastDetectedVideoUrl = null

        tabManager.getCurrentTab()?.loadUrl(formattedUrl)
    }

    private fun downloadVideo(url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setDescription("Downloading Video...")
            request.setTitle(url.substringAfterLast('/'))
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "dtech_video_${System.currentTimeMillis()}.mp4")

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            Toast.makeText(this, "Video Download Started", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Download Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        val currentTab = tabManager.getCurrentTab()
        if (currentTab != null && currentTab.canGoBack()) {
            currentTab.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Incognito Mode: Clear everything
        tabManager.getCurrentTab()?.clearCache(true)
        tabManager.getCurrentTab()?.clearHistory()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }
}
