package com.example.apart.features.apartments.ui.list

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.apart.databinding.ItemApartmentBinding
import com.example.apart.features.apartments.data.ApartmentHolderItem

class ApartmentListAdapter(
    val ownItemListener: ApartmentItemClickListener
): RecyclerView.Adapter<ApartmentListAdapter.ApartmentHolder>() {

    var items: List<ApartmentHolderItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApartmentHolder {
        return ApartmentHolder(
            ItemApartmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ApartmentHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class ApartmentHolder(
        private val binding: ItemApartmentBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ApartmentHolderItem) = with(binding) {
            textAddress.text = item.address
            textPrice.text = item.price
            Glide.with(image.context)
                .load(Uri.parse(item.image))
                .centerCrop()
                .into(image)
            textInformation.text = item.information
            textUnderground.text = item.undergroundStation
            textLink.text = item.link
        }
    }
}

interface ApartmentItemClickListener {
    fun onItemClick(apartment: ApartmentHolderItem)
}