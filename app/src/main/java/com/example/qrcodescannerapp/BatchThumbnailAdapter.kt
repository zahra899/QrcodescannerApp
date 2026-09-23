package com.example.qrcodescannerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BatchThumbnailAdapter(
    private val items: MutableList<ScannedProduct>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<BatchThumbnailAdapter.ThumbViewHolder>() {

    inner class ThumbViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val tvSku: TextView = view.findViewById(R.id.tvSku)
        val btnRemove: ImageView = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_batch_thumbnail, parent, false)
        return ThumbViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThumbViewHolder, position: Int) {
        val item = items[position]
        holder.tvSku.text = item.sku
        item.thumbnailBitmap?.let { holder.ivThumbnail.setImageBitmap(it) }

        holder.btnRemove.setOnClickListener {
            val removePos = holder.bindingAdapterPosition
            if (removePos != RecyclerView.NO_POSITION) {
                items.removeAt(removePos)
                notifyItemRemoved(removePos)
                onRemove(removePos)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}