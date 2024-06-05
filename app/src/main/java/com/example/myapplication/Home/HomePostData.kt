package com.example.myapplication.Home

import java.io.Serializable

data class HomePostData(
    val post_image: String = "",
    val post_place_name: String = "",
    val post_address: String = "",
    val post_time: Long = 0L,
    val post_rest_time: Int = 0,
    val post_price: String = "",
    val post_original_price: String = "",
    val userId: String = "" // 사용자 ID 추가
) : Serializable

