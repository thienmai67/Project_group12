package com.example.project_group12

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_group12.data.AppDao
import com.example.project_group12.data.AppDatabase
import com.example.project_group12.databinding.ActivitySearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var historyManager: SearchHistoryManager
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var songAdapter: SongAdapter
    private lateinit var dao: AppDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dao = AppDatabase.getDatabase(this).appDao()
        historyManager = SearchHistoryManager(this)

        setupRecyclerViews()
        setupListeners()
        loadHistory()
    }

    private fun setupRecyclerViews() {
        // Cài đặt Adapter cho Lịch sử
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        historyAdapter = HistoryAdapter(
            emptyList(),
            onItemClick = { query ->
                binding.edtSearchInput.setText(query)
                binding.edtSearchInput.setSelection(query.length)
                performSearch(query)
            },
            onDeleteClick = { query ->
                historyManager.removeQuery(query)
                loadHistory()
            }
        )
        binding.rvHistory.adapter = historyAdapter

        // Cài đặt Adapter cho Kết quả tìm kiếm (Dùng chung SongAdapter của trang chủ)
        binding.rvSearchResults.layoutManager = LinearLayoutManager(this)
        songAdapter = SongAdapter(emptyList()) { clickedSong ->
            // Thêm từ khóa vào lịch sử khi người dùng bấm nghe bài hát đó
            val query = binding.edtSearchInput.text.toString().trim()
            if (query.isNotEmpty()) historyManager.addQuery(query)

            MusicPlayerManager.playSong(clickedSong) {}
            startActivity(Intent(this, PlayerActivity::class.java))
        }
        binding.rvSearchResults.adapter = songAdapter
    }

    private fun setupListeners() {
        binding.btnSearchBack.setOnClickListener { finish() }

        // Bấm nút xóa tất cả lịch sử
        binding.btnClearHistory.setOnClickListener {
            historyManager.clearHistory()
            loadHistory()
        }

        // Bấm dấu X trên thanh tìm kiếm
        binding.btnClearInput.setOnClickListener {
            binding.edtSearchInput.text.clear()
        }

        // Lắng nghe thay đổi trên ô nhập liệu
        binding.edtSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    binding.btnClearInput.visibility = View.GONE
                    showHistoryView()
                } else {
                    binding.btnClearInput.visibility = View.VISIBLE
                    performSearch(query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Bắt sự kiện bấm nút "Tìm kiếm" trên bàn phím ảo
        binding.edtSearchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.edtSearchInput.text.toString().trim()
                if (query.isNotEmpty()) {
                    historyManager.addQuery(query)
                    performSearch(query)
                }
                true
            } else {
                false
            }
        }
    }

    private fun loadHistory() {
        val history = historyManager.getHistory()
        historyAdapter.updateData(history)
        if (history.isEmpty()) {
            binding.layoutHistory.visibility = View.GONE
        } else if (binding.edtSearchInput.text.isEmpty()) {
            binding.layoutHistory.visibility = View.VISIBLE
        }
    }

    private fun showHistoryView() {
        loadHistory()
        binding.rvSearchResults.visibility = View.GONE
        binding.tvNoResults.visibility = View.GONE
    }

    private fun performSearch(query: String) {
        binding.layoutHistory.visibility = View.GONE
        binding.rvSearchResults.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            // Sử dụng hàm searchSongs đã có sẵn trong AppDao của bạn
            val results = dao.searchSongs(query)

            withContext(Dispatchers.Main) {
                if (results.isEmpty()) {
                    binding.rvSearchResults.visibility = View.GONE
                    binding.tvNoResults.visibility = View.VISIBLE
                } else {
                    binding.rvSearchResults.visibility = View.VISIBLE
                    binding.tvNoResults.visibility = View.GONE
                    songAdapter.updateData(results)
                }
            }
        }
    }
}