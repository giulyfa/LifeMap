package com.example.lifemap.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val cognome: String,
    val email: String,
    val password: String,
    val lastLogin: Long? = null,
    val profilePhotoUri: String? = null,
    val logged: Boolean = false
)