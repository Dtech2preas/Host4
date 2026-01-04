package com.dtech.browser

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout

class TabManager(private val context: Context, private val container: FrameLayout) {
    private val tabs = mutableListOf<WebView>()
    private var currentTabIndex = -1

    fun createTab(url: String) {
        val webView = WebView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            loadUrl(url)
        }

        tabs.add(webView)
        switchToTab(tabs.size - 1)
    }

    fun switchToTab(index: Int) {
        if (index in tabs.indices) {
            container.removeAllViews()
            val webView = tabs[index]
            container.addView(webView)
            currentTabIndex = index
        }
    }

    fun getCurrentTab(): WebView? {
        if (currentTabIndex in tabs.indices) {
            return tabs[currentTabIndex]
        }
        return null
    }

    fun closeCurrentTab() {
        if (currentTabIndex != -1) {
            val webView = tabs[currentTabIndex]
            container.removeView(webView)
            webView.destroy()
            tabs.removeAt(currentTabIndex)

            if (tabs.isNotEmpty()) {
                val newIndex = if (currentTabIndex >= tabs.size) tabs.size - 1 else currentTabIndex
                switchToTab(newIndex)
            } else {
                currentTabIndex = -1
                // Potentially create a new empty tab or exit
                createTab("about:blank")
            }
        }
    }

    fun getTabCount(): Int = tabs.size
}
