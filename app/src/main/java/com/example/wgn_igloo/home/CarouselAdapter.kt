package com.example.wgn_igloo.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wgn_igloo.MainActivity
import com.example.wgn_igloo.R

class CarouselAdapter(
    private val mData: MutableList<ItemData>, private val context: Context, private val listener: OnItemClickListener, initialSelectedPosition: Int = 0) : RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    var selectedItemPosition = initialSelectedPosition

    interface OnItemClickListener {
        fun onItemClicked(position: Int)
    }
    data class ItemData(val imageResId: Int, val text: String, var isSelected: Boolean = false)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.carousel_image)
        val textView: TextView = view.findViewById(R.id.carousel_text)

        init {
            view.setOnClickListener {
                val previousItem = selectedItemPosition
                selectedItemPosition = adapterPosition
                notifyItemChanged(previousItem)
                notifyItemChanged(adapterPosition)
                val category = mData[adapterPosition % mData.size].text
                (context as? MainActivity)?.let {
                    ViewModelProvider(it).get(CarouselViewModel::class.java).selectCategory(category)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.carousel_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position % mData.size]

        Glide.with(context)
            .load(item.imageResId)
            .into(holder.imageView)

        holder.textView.text = item.text

        // Adjust the view scale based on the selection
        if (selectedItemPosition == position) {
            holder.itemView.scaleX = 1.2f
            holder.itemView.scaleY = 1.2f
        } else {
            holder.itemView.scaleX = 1.0f
            holder.itemView.scaleY = 1.0f
        }
    }

    override fun getItemCount(): Int = Integer.MAX_VALUE
}