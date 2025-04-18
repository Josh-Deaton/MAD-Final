package com.example.mobileappdev2025

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileInputStream
import java.util.Random
import java.util.Scanner

class MainActivity : AppCompatActivity()
{ // Write code for group chats. Agree on an application name. Meet up to work on it together.
    private lateinit var firebaseAuth : FireBaseaUTH
    private lateinit var databaase : FirebaseFireStore

    }
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // val user = firebaseAuth.CurrentUser
    }

    var hardcodeJson = "{\"Eric\": { \"age\": 28}}"

    var jo = JSONObject(hardcodeJson)
    var eric = jo.getJSONObject(name:"Eric")

    if (eric.has(name:"age"))
        var age = eric.getInt(name:"age") ?: -1

    private fun getConnections(_userID : String): Task<List<Strings>
    {
        val connectionRef = database.collection(collectionPath:"contections").document

        return connectionRef.get().continueWith { task ->
            val document  = task.result

            if (document.exists())
            {
                val data = document.data
                List<String> people = data?.get("people") as? List<String> ?:_emptyList()
            }
        }
    }
}