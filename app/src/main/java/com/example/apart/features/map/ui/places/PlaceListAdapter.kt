package com.example.apart.features.map.ui.places

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.apart.databinding.ItemPlaceBinding
import com.example.apart.features.map.data.room.PlaceEntity
import com.yandex.mapkit.geometry.Point

class PlaceListAdapter(val ownItemListener: OwnItemClickListener) :
    RecyclerView.Adapter<PlaceListAdapter.PlaceHolder>() {
    var items: List<PlaceHolderItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        return PlaceHolder(
            ItemPlaceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) =
        holder.bind(items[position])

    inner class PlaceHolder(
        private val binding: ItemPlaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlaceHolderItem) = with(binding) {
            textTitle.text = item.title
            textSubtitle.text = item.subtitle
            textPoint.text = "${item.point.latitude} ${item.point.longitude}"
            textImportance.text = item.importance.toString()

            buttonPlus.setOnClickListener { ownItemListener.onPlusClick(item) }
            buttonMinus.setOnClickListener { ownItemListener.onMinusClick(item) }
        }
    }
}

data class PlaceHolderItem(
    val title: String,
    val subtitle: String,
    val point: Point,
    var importance: Int = 0
) {
    fun toEntity() = PlaceEntity(
        title, subtitle, point.latitude, point.longitude, importance
    )
}

interface OwnItemClickListener {
    fun onPlusClick(place: PlaceHolderItem)

    fun onMinusClick(place: PlaceHolderItem)
}

