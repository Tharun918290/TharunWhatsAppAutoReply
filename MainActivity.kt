package com.tharun.autoreply

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 50, 40, 40)
            setBackgroundColor(Color.BLACK)
        }
        val title = TextView(this).apply {
            text = "Tharun Auto Reply"
            textSize = 28f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 25)
        }
        val info = TextView(this).apply {
            text = "WhatsApp keyword auto-reply\n\nKeywords:\n• requriment\n• requirement\n• recruitment\n\nCase-insensitive\nReply: tharun\nScope: all WhatsApp chats and groups\nFrequency: every matching notification"
            textSize = 17f
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, 30)
        }
        val settingsButton = Button(this).apply {
            text = "Enable Notification Access"
            setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        }
        val whatsappButton = Button(this).apply {
            text = "Open WhatsApp"
            setOnClickListener { packageManager.getLaunchIntentForPackage("com.whatsapp")?.let(::startActivity) }
        }
        root.addView(title); root.addView(info); root.addView(settingsButton); root.addView(whatsappButton)
        setContentView(root)
    }
}
