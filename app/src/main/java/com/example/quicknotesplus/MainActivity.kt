package com.example.quicknotesplus

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var adapter: NoteAdapter
    private lateinit var toolbar: Toolbar

    private lateinit var sharedPref: SharedPreferences
    private var currentTheme = "light"

    private val allNotesList = mutableListOf<Note>()
    private val displayedNotesList = mutableListOf<Note>()

    companion object {
        const val REQUEST_CODE_ADD_NOTE = 100
        const val REQUEST_CODE_EDIT_NOTE = 101
        const val PREFS_NAME = "app_settings"
        const val KEY_THEME = "theme"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Загружаем тему перед setContentView
        sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        currentTheme = sharedPref.getString(KEY_THEME, "light") ?: "light"
        applyTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupToolbar()
        loadNotesFromStorage()
        setupNotesList()
        setupSwipeToDelete()
        setupClickListeners()
        setupSearch()
    }

    private fun initViews() {
        fabAddNote = findViewById(R.id.fabAddNote)
        notesRecyclerView = findViewById(R.id.notesListView)
        searchBar = findViewById(R.id.searchBar)
        toolbar = findViewById(R.id.toolbar)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun applyTheme() {
        try {
            when (currentTheme) {
                "light" -> setTheme(R.style.Theme_QuickNotesPlus_Light)
                "dark" -> setTheme(R.style.Theme_QuickNotesPlus_Dark)
                else -> setTheme(R.style.Theme_QuickNotesPlus_Light)
            }
        } catch (e: Exception) {
            // Если темы нет, используем стандартную
            setTheme(android.R.style.Theme_Material_Light)
        }
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

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            private val deleteIcon = ContextCompat.getDrawable(
                this@MainActivity,
                android.R.drawable.ic_menu_delete
            )
            private val background = ColorDrawable(Color.RED)

            init {
                deleteIcon?.setTint(Color.WHITE)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position >= 0 && position < displayedNotesList.size) {
                    val deletedNote = displayedNotesList[position]

                    // Удаляем из обоих списков
                    displayedNotesList.removeAt(position)
                    allNotesList.remove(deletedNote)
                    adapter.notifyItemRemoved(position)

                    saveNotesToStorage()

                    // Показываем Snackbar с возможностью отмены
                    Snackbar.make(notesRecyclerView, "Заметка удалена", Snackbar.LENGTH_LONG)
                        .setAction("ОТМЕНИТЬ") {
                            // Восстанавливаем заметку
                            displayedNotesList.add(position, deletedNote)
                            allNotesList.add(position, deletedNote)
                            adapter.notifyItemInserted(position)
                            saveNotesToStorage()
                            Toast.makeText(
                                this@MainActivity,
                                "Заметка восстановлена",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .show()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon!!.intrinsicHeight) / 2

                when {
                    dX > 0 -> { // Свайп вправо
                        background.setBounds(
                            itemView.left,
                            itemView.top,
                            itemView.left + dX.toInt(),
                            itemView.bottom
                        )
                        deleteIcon.setBounds(
                            itemView.left + iconMargin,
                            itemView.top + iconMargin,
                            itemView.left + iconMargin + deleteIcon.intrinsicWidth,
                            itemView.top + iconMargin + deleteIcon.intrinsicHeight
                        )
                    }
                    dX < 0 -> { // Свайп влево
                        background.setBounds(
                            itemView.right + dX.toInt(),
                            itemView.top,
                            itemView.right,
                            itemView.bottom
                        )
                        deleteIcon.setBounds(
                            itemView.right - iconMargin - deleteIcon.intrinsicWidth,
                            itemView.top + iconMargin,
                            itemView.right - iconMargin,
                            itemView.top + iconMargin + deleteIcon.intrinsicHeight
                        )
                    }
                    else -> {
                        background.setBounds(0, 0, 0, 0)
                    }
                }

                background.draw(c)
                deleteIcon.draw(c)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(notesRecyclerView)
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

    // Меню для смены темы (будет в Toolbar)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_theme -> {
                showThemeDialog()
                true
            }
            R.id.action_settings -> {
                Toast.makeText(this, "Настройки", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Светлая тема", "Темная тема")
        val currentIndex = if (currentTheme == "light") 0 else 1

        AlertDialog.Builder(this)
            .setTitle("Выберите тему оформления")
            .setSingleChoiceItems(themes, currentIndex) { dialog, which ->
                val newTheme = if (which == 0) "light" else "dark"

                if (newTheme != currentTheme) {
                    currentTheme = newTheme

                    // Сохраняем выбор
                    sharedPref.edit().putString(KEY_THEME, newTheme).apply()

                    // Перезапускаем активити для применения темы
                    recreate()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
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
}