package com.example.quicknotesplus

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DataManager {
    private const val PREFS_NAME = "notes_prefs"
    private const val KEY_NOTES = "notes_list"
    private const val KEY_TAGS = "tags_list"
    private val gson = Gson()

    fun saveNotes(context: Context, notes: List<MainActivity.Note>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(notes)
        prefs.edit().putString(KEY_NOTES, json).apply()
    }

    fun loadNotes(context: Context): List<MainActivity.Note> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_NOTES, null)
        return if (json != null) {
            val type = object : TypeToken<List<MainActivity.Note>>() {}.type
            gson.fromJson(json, type) ?: getDefaultNotes(context)
        } else {
            getDefaultNotes(context)
        }
    }


    fun saveTags(context: Context, tags: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(tags)
        prefs.edit().putString(KEY_TAGS, json).apply()
    }

    fun loadTags(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TAGS, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type) ?: getDefaultTags()
        } else {
            getDefaultTags()
        }
    }


    private fun getDefaultNotes(context: Context): List<MainActivity.Note> {
        return listOf(
            MainActivity.Note(
                context.getString(R.string.welcome_title),
                context.getString(R.string.welcome_content),
                context.getString(R.string.instruction_tag)
            ),
            MainActivity.Note(
                context.getString(R.string.shopping_title),
                context.getString(R.string.shopping_content),
                context.getString(R.string.personal_tag)
            ),
            MainActivity.Note(
                context.getString(R.string.work_title),
                context.getString(R.string.work_content),
                context.getString(R.string.work_tag)
            )
        )
    }


    private fun getDefaultTags(): List<String> {
        return listOf("Работа", "Личное", "Покупки", "Учеба", "Идеи")
    }
}