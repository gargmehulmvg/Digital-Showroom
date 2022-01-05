package com.digitaldukaan.views

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class OverlapDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount
        if (position == (itemCount - 1)) outRect.set(0, 0, 0, 0) else outRect.set(-25, 0, 0, 0)
    }
}