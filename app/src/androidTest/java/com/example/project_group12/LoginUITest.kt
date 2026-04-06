package com.example.project_group12

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUITest {

    // Khởi chạy LoginActivity trước mỗi bài Test
    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun testLogin_EmptyFields_ShowsError() {
        // 1. Để trống Tên đăng nhập và Pass, bấm Đăng Nhập
        onView(withId(R.id.btnLogin)).perform(click())

        // Kiểm tra xem progressBar có đang ẩn không (vì không có dữ liệu nên không chạy lên Firebase)
        onView(withId(R.id.progressBar)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun testLogin_InputsUsername_Successfully() {
        // 1. Nhập Tên đăng nhập (Đã đổi ID từ edtEmail -> edtUsername)
        onView(withId(R.id.edtUsername))
            .perform(typeText("thienmai67"), closeSoftKeyboard())

        // 2. Nhập Mật khẩu
        onView(withId(R.id.edtPassword))
            .perform(typeText("123456"), closeSoftKeyboard())

        // 3. Kiểm tra xem chữ đã được nhập đúng vào ô chưa
        onView(withId(R.id.edtUsername)).check(matches(withText("thienmai67")))

        // 4. Bấm nút đăng nhập
        onView(withId(R.id.btnLogin)).perform(click())
    }

    @Test
    fun testNavigateToRegister() {
        // Kiểm tra tính năng chuyển trang: Bấm vào dòng "Chưa có tài khoản? Đăng ký ngay"
        onView(withId(R.id.tvGoToRegister)).perform(click())

        // Kiểm tra xem màn hình RegisterActivity có hiển thị không bằng cách tìm nút Tạo tài khoản
        onView(withId(R.id.btnRegisterSubmit)).check(matches(isDisplayed()))
    }
}