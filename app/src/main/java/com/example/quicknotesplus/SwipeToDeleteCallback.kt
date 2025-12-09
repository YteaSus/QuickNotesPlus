package com.example.quicknotesplus

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class SwipeToDeleteCallback(private val context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val deleteIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete)
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
            dX > 0 -> {
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
            dX < 0 -> {
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
        c.save()

        if (dX > 0) {
            c.clipRect(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
        } else {
            c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        }

        deleteIcon.draw(c)
        c.restore()
    }
}