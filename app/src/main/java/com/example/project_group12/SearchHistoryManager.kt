package com.example.project_group12

import android.content.Context
import android.content.SharedPreferences

class SearchHistoryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("SearchPrefs", Context.MODE_PRIVATE)

    fun getHistory(): List<String> {
        val str = prefs.getString("history_str", "") ?: ""
        return if (str.isEmpty()) emptyList() else str.split("|||")
    }

    fun addQuery(query: String) {
        if (query.isBlank()) return
        val list = getHistory().toMutableList()
        list.remove(query) // Xóa nếu đã tồn tại để đưa lên đầu
        list.add(0, query) // Thêm vào đầu danh sách

        // --- ĐÃ SỬA DÒNG NÀY ---
        if (list.size > 10) list.removeAt(list.lastIndex) // Thay vì removeLast()

        prefs.edit().putString("history_str", list.joinToString("|||")).apply()
    }

    fun removeQuery(query: String) {
        val list = getHistory().toMutableList()
        list.remove(query)
        prefs.edit().putString("history_str", list.joinToString("|||")).apply()
    }

    fun clearHistory() {
        prefs.edit().remove("history_str").apply()
    }
}