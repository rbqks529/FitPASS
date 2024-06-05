package com.example.myapplication.Home

import android.service.autofill.UserData
import java.io.Serializable

data class HomePostData(
    val post_image: String = "",
    val post_place_name: String = "",
    val post_address: String = "",
    val post_time: Long = 0L,
    val post_rest_time: Int = 0,
    val post_price: String = "",
    val post_original_price: String = "",
    val userId: String = "",
    val post_content: String = "" // 게시글 내용 필드 추가
) : Serializable

