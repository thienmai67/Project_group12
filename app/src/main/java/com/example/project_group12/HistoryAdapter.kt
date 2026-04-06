package com.example.project_group12

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private var historyList: List<String>,
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuery: TextView = view.findViewById(R.id.tvHistoryQuery)
        val btnRemove: ImageView = view.findViewById(R.id.btnRemoveHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val query = historyList[position]
        holder.tvQuery.text = query

        holder.itemView.setOnClickListener { onItemClick(query) }
        holder.btnRemove.setOnClickListener { onDeleteClick(query) }
    }

    override fun getItemCount() = historyList.size

    fun updateData(newList: List<String>) {
        historyList = newList
        notifyDataSetChanged()
    }
}