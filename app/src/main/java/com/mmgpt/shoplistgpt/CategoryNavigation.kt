package com.mmgpt.shoplistgpt

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView

/** Resolve the visible row against the current model; keep mode-specific paging at the caller. */
internal fun RecyclerView.setCategoryNavigation(
    categories: () -> List<ShoppingCategory>,
    navigate: (Int) -> Unit
) {
    val detector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Events delivered to this RecyclerView already use its local coordinates.
            val child = findChildViewUnder(e.x, e.y) ?: return false
            val categoryId = getChildViewHolder(child).itemId
            val index = categories().indexOfFirst { it.id == categoryId }
            if (index < 0) return false
            navigate(index)
            return true
        }
    })
    setOnTouchListener { _, event ->
        detector.onTouchEvent(event)
        false
    }
}
