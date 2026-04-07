package com.example.project_group12

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project_group12.data.UserModel

class UserAdapter(
    private var users: List<UserModel>,
    private val onRoleToggle: (UserModel) -> Unit,
    private val onDeleteClick: (UserModel) -> Unit // QUAN TRỌNG: Đã thêm tham số này
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvRole: TextView = view.findViewById(R.id.tvRole)
        val btnToggleRole: Button = view.findViewById(R.id.btnToggleRole)
        val btnDeleteUser: ImageView = view.findViewById(R.id.btnDeleteUser) // Khai báo nút xóa
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUsername.text = user.displayName
        holder.tvRole.text = "Vai trò: ${user.role.uppercase()}"

        // Xử lý đổi màu và chữ cho nút Cấp/Hủy quyền
        if (user.role == "admin") {
            holder.btnToggleRole.text = "Hủy Admin"
            holder.btnToggleRole.backgroundTintList = holder.itemView.context.getColorStateList(android.R.color.holo_red_dark)
        } else {
            holder.btnToggleRole.text = "Cấp Admin"
            holder.btnToggleRole.backgroundTintList = holder.itemView.context.getColorStateList(android.R.color.holo_green_dark)
        }

        // Bắt sự kiện click đổi quyền
        holder.btnToggleRole.setOnClickListener {
            onRoleToggle(user)
        }

        // Bắt sự kiện click xóa người dùng
        holder.btnDeleteUser.setOnClickListener {
            onDeleteClick(user)
        }
    }

    override fun getItemCount() = users.size

    fun updateData(newUsers: List<UserModel>) {
        users = newUsers
        notifyDataSetChanged()
    }
}