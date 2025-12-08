package com.example.quicknotesplus

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NoteEditActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: Button
    private lateinit var btnManageTags: Button
    private lateinit var tagsSpinner: AutoCompleteTextView

    private lateinit var availableTags: List<String>
    private var selectedTag: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        initViews()
        setupTagsSpinner()
        loadNoteData()
        setupClickListeners()
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        btnManageTags = findViewById(R.id.btnManageTags)
        tagsSpinner = findViewById(R.id.tagsSpinner)
    }

    private fun setupTagsSpinner() {
        availableTags = intent.getStringArrayListExtra("available_tags") ?:
                DataManager.loadTags(this)

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, availableTags)
        tagsSpinner.setAdapter(adapter)
        tagsSpinner.threshold = 1

        tagsSpinner.setOnItemClickListener { _, _, position, _ ->
            selectedTag = adapter.getItem(position) ?: getString(R.string.no_tag)
        }
    }

    private fun loadNoteData() {
        val noteTitle = intent.getStringExtra("title")
        val noteContent = intent.getStringExtra("content")
        val noteTag = intent.getStringExtra("tag")

        if (noteTitle != null) {
            etTitle.setText(noteTitle)
        }

        if (noteContent != null) {
            etContent.setText(noteContent)
        }

        if (noteTag != null && noteTag != getString(R.string.no_tag)) {
            tagsSpinner.setText(noteTag)
            selectedTag = noteTag
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            saveNote()
        }

        btnManageTags.setOnClickListener {
            val intent = Intent(this, TagsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun saveNote() {
        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val tag = tagsSpinner.text.toString().trim()
        val finalTag = if (tag.isNotEmpty()) tag else
            if (selectedTag.isNotEmpty()) selectedTag else getString(R.string.no_tag)

        if (title.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_title), Toast.LENGTH_SHORT).show()
            etTitle.requestFocus()
            return
        }

        if (content.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_content), Toast.LENGTH_SHORT).show()
            etContent.requestFocus()
            return
        }

        val resultIntent = Intent()
        resultIntent.putExtra("title", title)
        resultIntent.putExtra("content", content)
        resultIntent.putExtra("tag", finalTag)

        val position = intent.getIntExtra("position", -1)
        if (position != -1) {
            resultIntent.putExtra("position", position)
        }

        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        availableTags = DataManager.loadTags(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, availableTags)
        tagsSpinner.setAdapter(adapter)
    }
}