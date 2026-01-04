package com.dtech.browser

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import java.io.ByteArrayInputStream

class TabManager(
    private val context: Context,
    private val container: FrameLayout,
    private val onAdBlocked: () -> Unit,
    private val onVideoDetected: (String) -> Unit
) {
    private val tabs = mutableListOf<WebView>()
    private var currentTabIndex = -1
    private var isDesktopMode = false

    companion object {
        private const val DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    fun createTab(url: String) {
        val webView = WebView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true

            // Force Dark Mode if supported
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                settings.forceDark = WebSettings.FORCE_DARK_ON
            }

            if (isDesktopMode) {
                settings.userAgentString = DESKTOP_USER_AGENT
            }

            // Download Listener for standard files
            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                val request = DownloadManager.Request(Uri.parse(url))
                request.setMimeType(mimetype)
                request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
                request.addRequestHeader("User-Agent", userAgent)
                request.setDescription("Downloading file...")
                request.setTitle(url.substringAfterLast('/'))
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url.substringAfterLast('/'))

                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(context, "Download Started", Toast.LENGTH_LONG).show()
            }

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    val url = request?.url?.toString() ?: return null

                    if (AdBlocker.shouldBlock(url)) {
                        onAdBlocked()
                        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
                    }

                    if (url.endsWith(".mp4", ignoreCase = true) || url.endsWith(".m3u8", ignoreCase = true)) {
                        onVideoDetected(url)
                    }

                    return null
                }

                // Deprecated overload for older APIs if needed, though minSdk 24 supports the above
                override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
                    if (url == null) return null

                    if (AdBlocker.shouldBlock(url)) {
                        onAdBlocked()
                        return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream("".toByteArray()))
                    }

                    if (url.endsWith(".mp4", ignoreCase = true) || url.endsWith(".m3u8", ignoreCase = true)) {
                        onVideoDetected(url)
                    }

                    return null
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Inject CSS for Dark/Cyan theme
                    val js = "javascript:(function() { " +
                            "document.body.style.backgroundColor = '#000000'; " +
                            "document.body.style.color = '#00FFFF'; " +
                            "})()"
                    view?.evaluateJavascript(js, null)
                }
            }
            webChromeClient = WebChromeClient()
            loadUrl(url)
        }

        tabs.add(webView)
        switchToTab(tabs.size - 1)
    }

    fun toggleDesktopMode() {
        isDesktopMode = !isDesktopMode
        val currentTab = getCurrentTab() ?: return
        val currentUrl = currentTab.url

        // Update settings for all tabs or just current? Usually user expects global toggle.
        // For now, let's update the current tab and reload.
        // Ideally we should update all tabs or set a flag for future tabs.
        // We set the flag `isDesktopMode` so future tabs respect it.

        val newUA = if (isDesktopMode) DESKTOP_USER_AGENT else null // null resets to default

        // We need to apply this to the current WebView
        currentTab.settings.userAgentString = newUA
        if (currentUrl != null) {
            currentTab.loadUrl(currentUrl)
        }

        val modeText = if (isDesktopMode) "PC Mode ON" else "Mobile Mode ON"
        Toast.makeText(context, modeText, Toast.LENGTH_SHORT).show()
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
                createTab("about:blank")
            }
        }
    }

    fun getTabCount(): Int = tabs.size
}
