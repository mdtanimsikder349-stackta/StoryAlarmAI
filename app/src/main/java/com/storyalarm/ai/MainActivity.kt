package com.storyalarm.ai

import android.Manifest
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createChannel(this)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }
        setContent { StoryAlarmScreen() }
    }
}

private const val CHANNEL_ID = "story_alarm_alerts"

private fun createChannel(context: Context) {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channel = NotificationChannel(
        CHANNEL_ID, "Story Alarm alerts", NotificationManager.IMPORTANCE_HIGH
    ).apply { description = "Alerts configured in Story Alarm AI" }
    manager.createNotificationChannel(channel)
}

@Composable
fun StoryAlarmScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("story_alarm", Context.MODE_PRIVATE) }
    var username by remember { mutableStateOf(prefs.getString("username", "") ?: "") }
    var audioUri by remember { mutableStateOf(prefs.getString("audio_uri", null)) }
    var status by remember { mutableStateOf("এখনো কোনো Story-monitoring সংযোগ নেই।") }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    val audioPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            audioUri = uri.toString()
            prefs.edit().putString("audio_uri", audioUri).apply()
            status = "অ্যালার্মের গান নির্বাচন করা হয়েছে।"
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Story Alarm AI", style = MaterialTheme.typography.headlineMedium)
                Text("একজন Public Instagram অ্যাকাউন্টের জন্য ব্যক্তিগত অ্যালার্ম সেটআপ")
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it.trimStart('@') },
                    label = { Text("Instagram Username") },
                    placeholder = { Text("যেমন: username") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        prefs.edit().putString("username", username).apply()
                        status = if (username.isBlank()) "আগে Username লিখুন।"
                        else "Username সেভ হয়েছে: @$username"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Username সেভ করো") }

                OutlinedButton(
                    onClick = { audioPicker.launch(arrayOf("audio/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (audioUri == null) "ফোন থেকে অ্যালার্মের গান বেছে নাও" else "অন্য গান নির্বাচন করো") }

                Button(
                    onClick = {
                        try {
                            player?.release()
                            player = if (audioUri != null) {
                                MediaPlayer().apply {
                                    setDataSource(context, Uri.parse(audioUri))
                                    isLooping = true
                                    prepare()
                                    start()
                                }
                            } else null
                            if (player == null) {
                                android.media.MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI)
                                    ?.also { it.isLooping = true; it.start(); player = it }
                            }
                            status = "টেস্ট অ্যালার্ম বাজছে। নিচের Stop চাপলে বন্ধ হবে।"
                            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                                .setContentTitle("Story Alarm AI")
                                .setContentText("এটি অ্যালার্মের টেস্ট নোটিফিকেশন।")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .build()
                            NotificationManagerCompat.from(context).notify(1, notification)
                        } catch (e: Exception) {
                            status = "গান চালু করা যায়নি: ${e.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("অ্যালার্ম টেস্ট করো") }

                OutlinedButton(
                    onClick = {
                        player?.stop()
                        player?.release()
                        player = null
                        status = "অ্যালার্ম বন্ধ।"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("গান বন্ধ করো") }

                Text(status, style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
                Text("গুরুত্বপূর্ণ সীমাবদ্ধতা", style = MaterialTheme.typography.titleMedium)
                Text(
                    "এই প্রাথমিক সংস্করণ Username সেভ করতে, গান বেছে নিতে এবং অ্যালার্ম পরীক্ষা করতে পারে। " +
                    "Instagram-এর অনুমোদিত API সাধারণত ইচ্ছেমতো অন্য Public অ্যাকাউন্টের Story দেয় না। " +
                    "তাই এই সংস্করণ এখনো Story নিজে থেকে শনাক্ত করে না এবং Story এলেই বাজানোর নিশ্চয়তা দেয় না।"
                )
            }
        }
    }
}
