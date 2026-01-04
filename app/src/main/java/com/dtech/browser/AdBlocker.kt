package com.dtech.browser

object AdBlocker {
    private val BLOCKED_KEYWORDS = listOf(
        "ads",
        "doubleclick",
        "analytics",
        "facebook",
        "googleadservices"
    )

    fun shouldBlock(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return BLOCKED_KEYWORDS.any { lowerUrl.contains(it) }
    }
}
