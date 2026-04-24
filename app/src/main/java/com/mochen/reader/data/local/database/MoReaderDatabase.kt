package com.mochen.reader.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mochen.reader.data.local.dao.*
import com.mochen.reader.data.local.entity.*

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        BookmarkEntity::class,
        NoteEntity::class,
        GroupEntity::class,
        ReadingProgressEntity::class,
        SearchIndexEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class MoReaderDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun groupDao(): GroupDao
    abstract fun readingProgressDao(): ReadingProgressDao
    abstract fun searchIndexDao(): SearchIndexDao

    companion object {
        const val DATABASE_NAME = "mo_reader_database"
    }
}
