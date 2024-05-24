package com.example.apart.features.map.ui.places

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PlaceItemDecoration(
    private val mDivider: Drawable,
    private val leftOffset: Int = 0,
    private val topOffset: Int = 0,
    private val rightOffset: Int = 0,
    private val bottomOffset: Int = 0
): RecyclerView.ItemDecoration() {
    private val mBounds: Rect = Rect()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
            .let { if (it == RecyclerView.NO_POSITION) return else it }

        parent.adapter?.let { adapter ->
            outRect.bottom =
                if (position == adapter.itemCount - 1) bottomOffset
                else bottomOffset

            outRect.top =
                if (position == 0) topOffset
                else topOffset

            outRect.left = leftOffset
            outRect.right = rightOffset
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        c.save()
        val left = 24 * pxInDp()
        val right = parent.width - left
        val childCount = parent.childCount
        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, mBounds)
            val bottom = mBounds.bottom + Math.round(child.translationY)
            val top: Int = bottom - mDivider.intrinsicHeight
            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }

        c.restore()
    }

    private fun pxInDp() = Resources.getSystem().displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT
}