package com.mochen.reader.domain.model

data class Group(
    val id: Long = 0,
    val name: String,
    val coverColor: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
