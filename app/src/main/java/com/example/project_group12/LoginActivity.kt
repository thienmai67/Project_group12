package com.example.project_group12

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.project_group12.data.AppDatabase
import com.example.project_group12.databinding.ActivityLoginBinding
import com.example.project_group12.repository.AuthRepository
import com.example.project_group12.viewmodel.AuthState
import com.example.project_group12.viewmodel.AuthViewModel
import com.example.project_group12.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getDatabase(this).appDao()
        val repository = AuthRepository(dao)
        viewModel = ViewModelProvider(this, AuthViewModelFactory(repository))[AuthViewModel::class.java]

        // --- ĐÃ SỬA: Admin hay User đều tự động vào thẳng MainActivity ---
        lifecycleScope.launch {
            val savedUser = repository.getCurrentUser()
            if (savedUser != null && savedUser.role != "guest") {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
                return@launch
            }
        }

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.edtUsername.text.toString().trim()
            val pass = binding.edtPassword.text.toString().trim()

            if (username.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Tên và Mật khẩu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(username, pass)
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvGuest.setOnClickListener {
            viewModel.loginAsGuest()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is AuthState.Success -> {
                        binding.progressBar.visibility = View.GONE

                        // --- ĐÃ SỬA: Đăng nhập thành công, ai cũng về trang chủ MainActivity ---
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    is AuthState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
}