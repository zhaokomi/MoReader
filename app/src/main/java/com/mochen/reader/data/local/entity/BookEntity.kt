package com.mochen.reader.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String = "未知作者",
    val filePath: String,
    val coverPath: String? = null,
    val format: String, // TXT, EPUB, MOBI, AZW3, PDF
    val fileSize: Long = 0,
    val importTime: Long = System.currentTimeMillis(),
    val lastReadTime: Long = 0,
    val lastReadChapter: Int = 0,
    val lastReadPosition: Int = 0,
    val totalChapters: Int = 0,
    val totalWords: Int = 0,
    val currentProgress: Float = 0f, // 0.0 - 1.0
    val encoding: String = "UTF-8",
    val groupId: Long = 0,
    val isFavorite: Boolean = false,
    val deletedAt: Long? = null
)
