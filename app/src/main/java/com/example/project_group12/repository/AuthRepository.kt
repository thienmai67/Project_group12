package com.example.project_group12.repository

import com.example.project_group12.data.AppDao
import com.example.project_group12.data.UserLocal
import com.example.project_group12.data.UserModel // BỔ SUNG: Import thư viện UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException // ĐÃ THÊM: Thư viện bắt lỗi trùng tài khoản
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(private val dao: AppDao) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Lấy danh sách toàn bộ người dùng từ Firestore
    suspend fun getAllUsers(): Result<List<UserModel>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("users").get().await()
            val users = snapshot.documents.mapNotNull { doc ->
                val uid = doc.id
                val username = doc.getString("username") ?: ""
                val displayName = doc.getString("displayName") ?: ""
                val role = doc.getString("role") ?: "user"

                UserModel(uid, username, displayName, role)
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cập nhật quyền của người dùng (Admin <-> User)
    suspend fun updateUserRole(uid: String, newRole: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(uid).update("role", newRole).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // THÊM VÀO: Xóa người dùng khỏi hệ thống (Firestore)
    suspend fun deleteUser(uid: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(uid).delete().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, pass: String): Result<UserLocal> = withContext(Dispatchers.IO) {
        try {
            val fakeEmail = "$username@musicapp.com"
            val result = auth.signInWithEmailAndPassword(fakeEmail, pass).await()
            val user = result.user ?: throw Exception("Sai tài khoản hoặc mật khẩu")

            var role = "user"
            var displayName = username
            val doc = firestore.collection("users").document(user.uid).get().await()
            if (doc.exists()) {
                role = doc.getString("role") ?: "user"
                displayName = doc.getString("displayName") ?: username
            }

            val userLocal = UserLocal(user.uid, fakeEmail, role, displayName)

            // CẬP NHẬT QUAN TRỌNG: Xóa sạch dữ liệu (đặc biệt là tài khoản Khách cũ) trước khi lưu user mới
            dao.clearUser()
            dao.saveUser(userLocal)

            Result.success(userLocal)
        } catch (e: Exception) {
            // Sửa lỗi chung chung thành thông báo thân thiện
            Result.failure(Exception("Sai tài khoản hoặc mật khẩu!"))
        }
    }

    suspend fun register(username: String, pass: String): Result<UserLocal> = withContext(Dispatchers.IO) {
        try {
            val fakeEmail = "$username@musicapp.com"
            // Firebase sẽ tự chặn nếu email admin@musicapp.com đã được tạo trước đó
            val result = auth.createUserWithEmailAndPassword(fakeEmail, pass).await()
            val user = result.user ?: throw Exception("Đăng ký thất bại")

            // LOGIC: Chỉ ai nhập đúng username là "admin" mới được làm admin, tạo 1 lần duy nhất!
            val assignedRole = if (username.lowercase() == "admin") "admin" else "user"

            val userData = hashMapOf("username" to username, "role" to assignedRole, "displayName" to username)
            firestore.collection("users").document(user.uid).set(userData).await()

            val userLocal = UserLocal(user.uid, fakeEmail, assignedRole, username)

            // CẬP NHẬT QUAN TRỌNG: Tương tự login, xóa sạch rác trước khi lưu
            dao.clearUser()
            dao.saveUser(userLocal)

            Result.success(userLocal)
        } catch (e: FirebaseAuthUserCollisionException) {
            // ĐÃ THÊM: Bắt lỗi trùng tên đăng nhập
            Result.failure(Exception("Tên tài khoản đã tồn tại. Vui lòng chọn tên khác!"))
        } catch (e: Exception) {
            Result.failure(Exception("Lỗi đăng ký: Xin thử lại sau!"))
        }
    }

    suspend fun loginAsGuest(): Result<UserLocal> = withContext(Dispatchers.IO) {
        try {
            dao.clearUser()
            // Tạo một session Khách ảo lưu vào Room DB
            val guestUser = UserLocal("guest_id", "guest@music.com", "guest", "Khách")
            dao.saveUser(guestUser)
            Result.success(guestUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): UserLocal? = withContext(Dispatchers.IO) {
        dao.getCurrentUser()
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            auth.signOut()
            dao.clearUser()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}