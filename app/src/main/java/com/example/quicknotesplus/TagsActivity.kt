package com.example.quicknotesplus

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TagsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var etNewTag: EditText
    private lateinit var btnAddTag: Button
    private lateinit var tagsRecyclerView: RecyclerView

    private val tagsList = mutableListOf<String>()
    private lateinit var adapter: TagAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tags)

        initViews()
        loadTagsFromStorage()
        setupTagsList()
        setupClickListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etNewTag = findViewById(R.id.etNewTag)
        btnAddTag = findViewById(R.id.btnAddTag)
        tagsRecyclerView = findViewById(R.id.tagsListView)
    }

    private fun loadTagsFromStorage() {
        tagsList.clear()
        tagsList.addAll(DataManager.loadTags(this))
    }

    private fun saveTagsToStorage() {
        DataManager.saveTags(this, tagsList)
    }

    private fun setupTagsList() {
        tagsRecyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TagAdapter(tagsList) { tag, position ->
            etNewTag.setText(tag)
            Toast.makeText(this, "Редактировать тег: $tag", Toast.LENGTH_SHORT).show()
        }

        tagsRecyclerView.adapter = adapter

        tagsRecyclerView.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnAddTag.setOnClickListener {
            addNewTag()
        }

        etNewTag.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                addNewTag()
                true
            } else {
                false
            }
        }
    }

    private fun addNewTag() {
        val newTag = etNewTag.text.toString().trim()

        if (newTag.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_tag_name), Toast.LENGTH_SHORT).show()
            etNewTag.requestFocus()
            return
        }

        if (tagsList.contains(newTag)) {
            Toast.makeText(this, getString(R.string.tag_exists), Toast.LENGTH_SHORT).show()
            return
        }

        tagsList.add(newTag)
        saveTagsToStorage()

        adapter.notifyItemInserted(tagsList.size - 1)
        tagsRecyclerView.scrollToPosition(tagsList.size - 1)

        etNewTag.text.clear()
        Toast.makeText(this, getString(R.string.tag_added, newTag), Toast.LENGTH_SHORT).show()
    }
}

class TagAdapter(
    private val tags: List<String>,
    private val onTagClick: (String, Int) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<TagAdapter.TagViewHolder>() {

    class TagViewHolder(itemView: android.view.View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        val textView: android.widget.TextView = itemView.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TagViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val tag = tags[position]
        holder.textView.text = tag

        holder.itemView.setOnClickListener {
            onTagClick(tag, position)
        }

        holder.itemView.setOnLongClickListener {
            Toast.makeText(it.context, "Долгое нажатие для удаления тега", Toast.LENGTH_SHORT).show()
            true
        }
    }

    override fun getItemCount(): Int = tags.size
}