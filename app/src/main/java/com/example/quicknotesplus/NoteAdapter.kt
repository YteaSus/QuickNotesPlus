package com.example.quicknotesplus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val notes: List<MainActivity.Note>,
    private val onNoteClick: (MainActivity.Note, Int) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.note_title)
        val contentTextView: TextView = itemView.findViewById(R.id.note_content)
        val tagTextView: TextView = itemView.findViewById(R.id.note_tag)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.titleTextView.text = note.title
        holder.contentTextView.text = note.content
        holder.tagTextView.text = note.tag

        holder.itemView.setOnClickListener {
            onNoteClick(note, position)
        }

        holder.itemView.setOnLongClickListener {
            android.widget.Toast.makeText(
                it.context,
                "Заметка: ${note.title}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            true
        }
    }

    override fun getItemCount(): Int = notes.size
}