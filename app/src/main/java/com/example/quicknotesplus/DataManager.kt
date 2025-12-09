package com.example.quicknotesplus

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object DataManager {

    private const val NOTES_FILE = "notes.json"
    private const val TAGS_FILE = "tags.json"
    private val gson = Gson()

    fun saveNotes(context: Context, notes: List<Note>) {
        val json = gson.toJson(notes)
        val file = File(context.filesDir, NOTES_FILE)
        file.writeText(json)
    }

    fun loadNotes(context: Context): List<Note> {
        val file = File(context.filesDir, NOTES_FILE)
        return if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<Note>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun saveTags(context: Context, tags: List<String>) {
        val json = gson.toJson(tags)
        val file = File(context.filesDir, TAGS_FILE)
        file.writeText(json)
    }

    fun loadTags(context: Context): List<String> {
        val file = File(context.filesDir, TAGS_FILE)
        return if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: listOf("Работа", "Учеба", "Личное")
        } else {
            listOf("Работа", "Учеба", "Личное")
        }
    }
}