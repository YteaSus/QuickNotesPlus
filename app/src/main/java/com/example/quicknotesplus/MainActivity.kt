package com.example.quicknotesplus

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var adapter: NoteAdapter

    private val allNotesList = mutableListOf<Note>()
    private val displayedNotesList = mutableListOf<Note>()

    companion object {
        const val REQUEST_CODE_ADD_NOTE = 100
        const val REQUEST_CODE_EDIT_NOTE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadNotesFromStorage()
        setupNotesList()
        setupClickListeners()
        setupSearch()
    }

    private fun initViews() {
        fabAddNote = findViewById(R.id.fabAddNote)
        notesRecyclerView = findViewById(R.id.notesListView)
        searchBar = findViewById(R.id.searchBar)
    }

    private fun loadNotesFromStorage() {
        val loadedNotes = DataManager.loadNotes(this)
        allNotesList.clear()
        allNotesList.addAll(loadedNotes)

        displayedNotesList.clear()
        displayedNotesList.addAll(allNotesList)
    }

    private fun saveNotesToStorage() {
        DataManager.saveNotes(this, allNotesList)
    }

    private fun setupNotesList() {
        notesRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter = NoteAdapter(displayedNotesList) { note, position ->
            openNoteForEditing(note, position)
        }

        notesRecyclerView.adapter = adapter

        notesRecyclerView.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
    }

    private fun setupClickListeners() {
        fabAddNote.setOnClickListener {
            val intent = Intent(this, NoteEditActivity::class.java)
            val tags = DataManager.loadTags(this)
            intent.putStringArrayListExtra("available_tags", ArrayList(tags))
            startActivityForResult(intent, REQUEST_CODE_ADD_NOTE)
        }

        fabAddNote.setOnLongClickListener {
            val intent = Intent(this, TagsActivity::class.java)
            startActivity(intent)
            true
        }
    }

    private fun setupSearch() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                performSearch(s.toString())
            }
        })
    }

    private fun performSearch(query: String) {
        displayedNotesList.clear()

        if (query.isEmpty()) {
            displayedNotesList.addAll(allNotesList)
        } else {
            val searchQuery = query.lowercase()
            allNotesList.forEach { note ->
                if (note.title.lowercase().contains(searchQuery) ||
                    note.content.lowercase().contains(searchQuery) ||
                    note.tag.lowercase().contains(searchQuery)) {
                    displayedNotesList.add(note)
                }
            }
        }

        adapter.notifyDataSetChanged()

        if (query.isNotEmpty()) {
            Toast.makeText(this, "Найдено: ${displayedNotesList.size} заметок", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openNoteForEditing(note: Note, position: Int) {
        val intent = Intent(this, NoteEditActivity::class.java)
        intent.putExtra("title", note.title)
        intent.putExtra("content", note.content)
        intent.putExtra("tag", note.tag)
        intent.putExtra("position", position)

        val tags = DataManager.loadTags(this)
        intent.putStringArrayListExtra("available_tags", ArrayList(tags))

        startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val title = data.getStringExtra("title") ?: ""
            val content = data.getStringExtra("content") ?: ""
            val tag = data.getStringExtra("tag") ?: getString(R.string.no_tag)

            when (requestCode) {
                REQUEST_CODE_ADD_NOTE -> {
                    val newNote = Note(title, content, tag)
                    allNotesList.add(newNote)
                    displayedNotesList.add(newNote)
                    adapter.notifyItemInserted(allNotesList.size - 1)

                    saveNotesToStorage()
                    searchBar.text.clear()
                    performSearch("")

                    Toast.makeText(this, getString(R.string.note_saved), Toast.LENGTH_SHORT).show()
                }

                REQUEST_CODE_EDIT_NOTE -> {
                    val position = data.getIntExtra("position", -1)
                    if (position != -1 && position < allNotesList.size) {
                        val updatedNote = Note(title, content, tag)
                        allNotesList[position] = updatedNote

                        val displayPosition = displayedNotesList.indexOfFirst {
                            it.title == allNotesList[position].title &&
                                    it.content == allNotesList[position].content
                        }
                        if (displayPosition != -1) {
                            displayedNotesList[displayPosition] = updatedNote
                            adapter.notifyItemChanged(displayPosition)
                        }

                        saveNotesToStorage()
                        Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotesFromStorage()
        performSearch(searchBar.text.toString())
    }

    data class Note(
        val title: String,
        val content: String,
        val tag: String
    )
}