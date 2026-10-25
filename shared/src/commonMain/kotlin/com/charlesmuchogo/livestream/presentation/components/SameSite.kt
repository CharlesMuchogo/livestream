package com.charlesmuchogo.livestream.presentation.components

fun siteKey(host: String?): String {
    val labels = host?.lowercase()?.removePrefix("www.")?.split('.')?.filter { it.isNotBlank() }.orEmpty()
    return labels.takeLast(2).joinToString(".")
}

fun hostOf(url: String): String? = url.substringAfter("://", "").substringBefore('/').substringBefore('?').substringBefore(':').ifBlank { null }

fun sameSite(host: String?, allowedHost: String): Boolean {
    val candidate = host?.lowercase()?.removePrefix("www.") ?: return false
    val allowed = allowedHost.lowercase().removePrefix("www.")
    if (allowed.isEmpty()) return true
    return candidate == allowed || candidate.endsWith(".$allowed") || allowed.endsWith(".$candidate")
}
