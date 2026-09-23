package com.example.qrcodescannerapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QrTypeAdapter(
    private var items: List<QrTypeOption>,
    private val onItemClick: (QrTypeOption) -> Unit
) : RecyclerView.Adapter<QrTypeAdapter.QrTypeViewHolder>() {

    inner class QrTypeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view.findViewById(R.id.cardTypeContainer)
        val box: LinearLayout = view.findViewById(R.id.boxTypeIcon)
        val icon: ImageView = view.findViewById(R.id.ivTypeIcon)
        val label: TextView = view.findViewById(R.id.tvTypeLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_qr_type, parent, false)
        return QrTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: QrTypeViewHolder, position: Int) {
        val item = items[position]
        holder.label.text = item.label
        holder.icon.setImageResource(item.iconRes)

        // Box ka rounded background color, icon apna original color rakhta hai (tint nahi lagana)
        val bg = holder.box.background.mutate()
        if (bg is android.graphics.drawable.GradientDrawable) {
            bg.setColor(Color.parseColor(item.bgColor))
        }

        holder.container.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<QrTypeOption>) {
        items = newItems
        notifyDataSetChanged()
    }
}