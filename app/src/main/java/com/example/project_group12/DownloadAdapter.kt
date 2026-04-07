package com.example.project_group12

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project_group12.data.Song

class DownloadAdapter(
    private var songs: List<Song>,
    private val onItemClick: (Song) -> Unit,
    private val onDeleteClick: (Song) -> Unit
) : RecyclerView.Adapter<DownloadAdapter.DownloadViewHolder>() {

    class DownloadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvDownloadTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvDownloadArtist)
        val imgCover: ImageView = view.findViewById(R.id.imgDownloadCover)
        val btnDelete: ImageView = view.findViewById(R.id.btnDownloadDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadViewHolder {
        // Trỏ đúng vào file layout mới tạo
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false)
        return DownloadViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadViewHolder, position: Int) {
        val song = songs[position]
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist

        Glide.with(holder.itemView.context)
            .load(song.coverUrl)
            .placeholder(android.R.drawable.ic_media_play)
            .into(holder.imgCover)

        // Bấm vào khu vực bài hát để phát nhạc
        holder.itemView.setOnClickListener { onItemClick(song) }

        // Bấm vào thùng rác để xóa
        holder.btnDelete.setOnClickListener { onDeleteClick(song) }
    }

    override fun getItemCount() = songs.size

    fun updateData(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}