package com.example.myapplication.Home

import java.io.Serializable

data class HomePostData (
    val post_image: Int,
    val post_place_name: String,
    val post_address: String,
    val post_time: String,
    val post_rest_time: String,
    val post_price: String,
    val post_original_price: String
) : Serializable

