package com.example.qrcodescannerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private var items: List<Any>,
    private val onFavClick: (HistoryItem) -> Unit,
    private val onLongClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private var isSelectionMode = false

    fun updateItems(newItems: List<Any>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun setSelectionMode(enabled: Boolean) {
        isSelectionMode = enabled
        if (!enabled) items.filterIsInstance<HistoryItem>().forEach { it.isSelected = false }
        notifyDataSetChanged()
    }

    fun selectAll() {
        items.filterIsInstance<HistoryItem>().forEach { it.isSelected = true }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<HistoryItem> =
        items.filterIsInstance<HistoryItem>().filter { it.isSelected }

    override fun getItemViewType(position: Int) =
        if (items[position] is String) TYPE_HEADER else TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history_header, parent, false))
        } else {
            ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history_row, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.tvHeader.text = items[position] as String
        } else if (holder is ItemViewHolder) {
            holder.bind(items[position] as HistoryItem, isSelectionMode, onFavClick, onLongClick)
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHeader: TextView = view.findViewById(R.id.tvHeaderTitle)
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvType: TextView = view.findViewById(R.id.tvType)
        private val ivFav: ImageView = view.findViewById(R.id.ivFavorite)
        private val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)

        fun bind(item: HistoryItem, isSelectionMode: Boolean, onFavClick: (HistoryItem) -> Unit, onLongClick: () -> Unit) {
            tvTitle.text = item.title
            tvType.text = item.type

            if (isSelectionMode) {
                ivFav.visibility = View.GONE
                cbSelect.visibility = View.VISIBLE
                cbSelect.setOnCheckedChangeListener(null)
                cbSelect.isChecked = item.isSelected
                cbSelect.setOnCheckedChangeListener { _, checked -> item.isSelected = checked }
            } else {
                cbSelect.visibility = View.GONE
                ivFav.visibility = View.VISIBLE
                if (item.isFavorite) {
                    ivFav.setImageResource(android.R.drawable.btn_star_big_on)
                    ivFav.setColorFilter(ContextCompat.getColor(itemView.context, android.R.color.holo_green_light))
                } else {
                    ivFav.setImageResource(android.R.drawable.btn_star_big_off)
                    ivFav.setColorFilter(ContextCompat.getColor(itemView.context, android.R.color.darker_gray))
                }
                ivFav.setOnClickListener { onFavClick(item) }
            }

            itemView.setOnLongClickListener { onLongClick(); true }
            itemView.setOnClickListener { if (isSelectionMode) cbSelect.isChecked = !cbSelect.isChecked }
        }
    }
}