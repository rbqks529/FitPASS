package com.example.myapplication.Post

import java.io.Serializable

data class UserData(
    val userId: String = "",
    val userName: String = "",
    val userProfileUrl: String = ""
) : Serializable