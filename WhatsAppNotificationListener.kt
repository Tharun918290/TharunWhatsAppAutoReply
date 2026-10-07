package com.tharun.autoreply

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class WhatsAppNotificationListener : NotificationListenerService() {
    companion object {
        private const val WHATSAPP_PACKAGE = "com.whatsapp"
        private const val REPLY_TEXT = "tharun"
        private val KEYWORDS = listOf("requriment", "requirement", "recruitment")
        private val processed = ConcurrentHashMap<String, Long>()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != WHATSAPP_PACKAGE) return
        val notification = sbn.notification ?: return
        val texts = extractMessageTexts(notification)
        if (texts.isEmpty()) return
        val combined = texts.joinToString("\n")
        if (!containsKeyword(combined)) return
        val key = "${sbn.key}|${sbn.postTime}|${combined.hashCode()}"
        if (processed.putIfAbsent(key, System.currentTimeMillis()) != null) return
        if (processed.size > 500) {
            val cutoff = System.currentTimeMillis() - 600_000L
            processed.entries.removeIf { it.value < cutoff }
        }
        replyToNotification(notification)
    }

    private fun containsKeyword(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        return KEYWORDS.any { keyword ->
            Regex("(?<![A-Za-z])${Regex.escape(keyword)}(?![A-Za-z])").containsMatchIn(lower)
        }
    }

    private fun extractMessageTexts(notification: Notification): List<String> {
        val result = mutableListOf<String>()
        val extras = notification.extras ?: Bundle()
        extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.takeIf { it.isNotBlank() }?.let(result::add)
        val messages = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
        messages?.forEach { item ->
            val bundle = item as? Bundle ?: return@forEach
            bundle.getCharSequence("text")?.toString()?.takeIf { it.isNotBlank() }?.let(result::add)
        }
        return result.distinct()
    }

    private fun replyToNotification(notification: Notification) {
        val action = notification.actions?.firstOrNull { it.remoteInputs?.any { input -> input.resultKey.isNotBlank() } == true } ?: return
        val input = action.remoteInputs?.firstOrNull { it.resultKey.isNotBlank() } ?: return
        val intent = Intent()
        val results = Bundle().apply { putCharSequence(input.resultKey, REPLY_TEXT) }
        RemoteInput.addResultsToIntent(arrayOf(input), intent, results)
        try { action.actionIntent.send(this, 0, intent) } catch (_: PendingIntent.CanceledException) { }
    }
}
