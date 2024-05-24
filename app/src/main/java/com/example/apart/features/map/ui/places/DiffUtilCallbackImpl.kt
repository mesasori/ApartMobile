package com.example.apart.features.map.ui.places

import androidx.recyclerview.widget.DiffUtil

class DiffUtilCallbackImpl(
    private val oldItems: List<PlaceHolderItem>,
    private val newItems: List<PlaceHolderItem>
): DiffUtil.Callback() {
    override fun getOldListSize() = oldItems.size
    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldItems[oldItemPosition]
        val newItem = newItems[newItemPosition]
        return oldItem == newItem
    }
}