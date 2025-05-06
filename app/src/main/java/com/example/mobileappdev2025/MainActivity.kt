package com.example.mobileappdev2025

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileInputStream
import java.util.Scanner

class MainActivity : AppCompatActivity() {
    private lateinit var courseSpinner: Spinner
    private lateinit var messageList: ListView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var studyGroupList: ListView
    private lateinit var createGroupButton: Button
    private lateinit var joinGroupButton: Button
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "study_group_channel"
    private val NOTIFICATION_ID = 1

    // Store joined groups and their messages
    private val joinedGroups = mutableSetOf<String>()
    private val groupMessages = mutableMapOf<String, MutableList<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Initialize notification manager for all Android versions
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Setup window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createNotificationChannel()
        initializeViews()
        setupCourseSpinner()
        setupMessageList()
        setupStudyGroupList()
        setupClickListeners()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val name = "Study Group Notifications"
                val descriptionText = "Notifications for study group messages"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                // Log error but don't crash the app
                e.printStackTrace()
            }
        }
    }

    private fun initializeViews() {
        try {
            courseSpinner = findViewById(R.id.courseSpinner)
            messageList = findViewById(R.id.messageList)
            messageInput = findViewById(R.id.messageInput)
            sendButton = findViewById(R.id.sendButton)
            studyGroupList = findViewById(R.id.studyGroupList)
            createGroupButton = findViewById(R.id.createGroupButton)
            joinGroupButton = findViewById(R.id.joinGroupButton)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupCourseSpinner() {
        val courses = arrayOf(
            "Computer Science",
            "Mathematics",
            "Physics",
            "Engineering",
            "Business",
            "Psychology"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        courseSpinner.adapter = adapter
    }

    private fun setupMessageList() {
        val messages = ArrayList<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, messages)
        messageList.adapter = adapter
    }

    private fun setupStudyGroupList() {
        val studyGroups = ArrayList<String>()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, studyGroups)
        studyGroupList.adapter = adapter
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                val course = courseSpinner.selectedItem.toString()
                sendMessage(course, message)
                messageInput.text.clear()
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        createGroupButton.setOnClickListener {
            val course = courseSpinner.selectedItem.toString()
            createStudyGroup(course)
        }

        joinGroupButton.setOnClickListener {
            showJoinGroupDialog()
        }

        studyGroupList.setOnItemClickListener { _, _, position, _ ->
            val selectedGroup = studyGroupList.adapter.getItem(position) as String
            if (joinedGroups.contains(selectedGroup)) {
                showGroupMessages(selectedGroup)
            } else {
                Toast.makeText(this, "Please join the group first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showJoinGroupDialog() {
        val adapter = studyGroupList.adapter as ArrayAdapter<String>
        val groups = ArrayList<String>()
        for (i in 0 until adapter.count) {
            adapter.getItem(i)?.let { groups.add(it as String) }
        }

        if (groups.isEmpty()) {
            Toast.makeText(this, "No study groups available to join", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Join Study Group")
            .setItems(groups.toTypedArray()) { _, which ->
                val selectedGroup = groups[which]
                joinStudyGroup(selectedGroup)
            }
            .show()
    }

    private fun joinStudyGroup(groupName: String) {
        if (joinedGroups.add(groupName)) {
            groupMessages[groupName] = mutableListOf()
            Toast.makeText(this, "Joined $groupName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Already joined $groupName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showGroupMessages(groupName: String) {
        try {
            val messages = groupMessages[groupName] ?: mutableListOf()
            val adapter = messageList.adapter as ArrayAdapter<String>
            adapter.clear()
            adapter.addAll(messages)
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading messages: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage(course: String, message: String) {
        try {
            val adapter = messageList.adapter as ArrayAdapter<String>
            val messageText = "[$course] $message"
            adapter.add(messageText)
            adapter.notifyDataSetChanged()

            // Add message to all joined groups of the same course
            joinedGroups.filter { it.startsWith(course) }.forEach { group ->
                groupMessages[group]?.add(messageText)
            }

            // Show notification
            showNotification(course, message)
            Toast.makeText(this, "Message sent to $course group", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error sending message: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotification(course: String, message: String) {
        try {
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New message in $course")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            // Log error but don't crash the app
            e.printStackTrace()
        }
    }

    private fun createStudyGroup(course: String) {
        try {
            val adapter = studyGroupList.adapter as ArrayAdapter<String>
            val groupName = "$course Study Group ${adapter.count + 1}"
            adapter.add(groupName)
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Created new study group: $groupName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating study group: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}