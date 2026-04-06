package com.example.project_group12

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.project_group12.data.AppDatabase
import com.example.project_group12.databinding.ActivityRegisterBinding
import com.example.project_group12.repository.AuthRepository
import com.example.project_group12.viewmodel.AuthState
import com.example.project_group12.viewmodel.AuthViewModel
import com.example.project_group12.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getDatabase(this).appDao()
        val repository = AuthRepository(dao)
        viewModel = ViewModelProvider(this, AuthViewModelFactory(repository))[AuthViewModel::class.java]

        binding.btnRegisterSubmit.setOnClickListener {
            val user = binding.edtRegUsername.text.toString().trim()
            val pass = binding.edtRegPassword.text.toString().trim()
            val confirm = binding.edtRegConfirmPassword.text.toString().trim()

            if (user.isEmpty() || pass.length < 6) {
                Toast.makeText(this, "Tên không trống, Mật khẩu ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(user, pass)
        }

        binding.tvGoBackLogin.setOnClickListener { finish() }

        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> binding.progressBarReg.visibility = View.VISIBLE
                    is AuthState.Success -> {
                        binding.progressBarReg.visibility = View.GONE

                        if (state.user.role == "admin") {
                            Toast.makeText(this@RegisterActivity, "Chào mừng Quản trị viên!", Toast.LENGTH_SHORT).show()
                        }

                        // --- ĐÃ SỬA: Đăng ký thành công xong thì về trang chủ chơi nhạc ---
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    is AuthState.Error -> {
                        binding.progressBarReg.visibility = View.GONE
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> binding.progressBarReg.visibility = View.GONE
                }
            }
        }
    }
}