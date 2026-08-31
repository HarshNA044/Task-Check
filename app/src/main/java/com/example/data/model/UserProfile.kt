package com.example.data.model

data class UserProfile(
    val id: String = "harshna63@gmail.com",
    val name: String = "Harshna",
    val email: String = "harshna63@gmail.com",
    val bio: String = "Productive & Focused",
    val isGoogleSignedIn: Boolean = true,
    val avatarInitial: String = "H",
    val photoUrl: String? = null
)
