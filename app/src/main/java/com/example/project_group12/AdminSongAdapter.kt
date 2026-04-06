package com.example.project_group12

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project_group12.data.Song

class AdminSongAdapter(
    private var songs: List<Song>,
    private val onEditClick: ((Song) -> Unit)? = null, // Cho phép null
    private val onDeleteClick: (Song) -> Unit
) : RecyclerView.Adapter<AdminSongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvAdminTitle)
        val tvArtist: TextView = view.findViewById(R.id.tvAdminArtist)
        val imgCover: ImageView = view.findViewById(R.id.imgAdminCover)
        val btnEdit: ImageView = view.findViewById(R.id.btnAdminEdit)
        val btnDelete: ImageView = view.findViewById(R.id.btnAdminDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song_admin, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.tvTitle.text = song.title
        holder.tvArtist.text = song.artist

        Glide.with(holder.itemView.context)
            .load(song.coverUrl)
            .placeholder(android.R.drawable.ic_media_play)
            .into(holder.imgCover)

        // Xử lý ẩn/hiện nút sửa
        if (onEditClick == null) {
            holder.btnEdit.visibility = View.GONE
        } else {
            holder.btnEdit.visibility = View.VISIBLE
            holder.btnEdit.setOnClickListener { onEditClick.invoke(song) }
        }

        holder.btnDelete.setOnClickListener { onDeleteClick(song) }
    }

    override fun getItemCount() = songs.size

    fun updateData(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
}