package com.mochen.reader.di

import android.content.Context
import androidx.room.Room
import com.mochen.reader.data.local.dao.*
import com.mochen.reader.data.local.database.MoReaderDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MoReaderDatabase {
        return Room.databaseBuilder(
            context,
            MoReaderDatabase::class.java,
            MoReaderDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideBookDao(database: MoReaderDatabase): BookDao {
        return database.bookDao()
    }

    @Provides
    @Singleton
    fun provideChapterDao(database: MoReaderDatabase): ChapterDao {
        return database.chapterDao()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(database: MoReaderDatabase): BookmarkDao {
        return database.bookmarkDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: MoReaderDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideGroupDao(database: MoReaderDatabase): GroupDao {
        return database.groupDao()
    }

    @Provides
    @Singleton
    fun provideReadingProgressDao(database: MoReaderDatabase): ReadingProgressDao {
        return database.readingProgressDao()
    }

    @Provides
    @Singleton
    fun provideSearchIndexDao(database: MoReaderDatabase): SearchIndexDao {
        return database.searchIndexDao()
    }
}
