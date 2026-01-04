package com.dtech.browser

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tabManager: TabManager
    private lateinit var etUrl: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val container = findViewById<FrameLayout>(R.id.webview_container)
        tabManager = TabManager(this, container)

        // Initialize UI controls
        etUrl = findViewById(R.id.et_url)
        val btnBack = findViewById<Button>(R.id.btn_back)
        val btnForward = findViewById<Button>(R.id.btn_forward)
        val btnGo = findViewById<Button>(R.id.btn_go)
        val btnTabs = findViewById<Button>(R.id.btn_tabs)

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
            // For MVP, just show a toast or create a new tab for testing
            tabManager.createTab("https://www.google.com")
            Toast.makeText(this, "New Tab Created. Total: ${tabManager.getTabCount()}", Toast.LENGTH_SHORT).show()
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
        tabManager.getCurrentTab()?.loadUrl(formattedUrl)
    }

    override fun onBackPressed() {
        val currentTab = tabManager.getCurrentTab()
        if (currentTab != null && currentTab.canGoBack()) {
            currentTab.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
