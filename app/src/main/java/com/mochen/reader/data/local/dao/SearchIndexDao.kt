package com.mochen.reader.data.local.dao

import androidx.room.*
import com.mochen.reader.data.local.entity.SearchIndexEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchIndexDao {

    @Query("SELECT * FROM search_index WHERE bookId = :bookId AND keyword LIKE '%' || :keyword || '%' ORDER BY chapterIndex ASC, position ASC")
    fun searchInBook(bookId: Long, keyword: String): Flow<List<SearchIndexEntity>>

    @Query("SELECT * FROM search_index WHERE keyword LIKE '%' || :keyword || '%' ORDER BY bookId ASC, chapterIndex ASC")
    fun searchAll(keyword: String): Flow<List<SearchIndexEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchIndex(index: SearchIndexEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchIndexes(indexes: List<SearchIndexEntity>)

    @Query("DELETE FROM search_index WHERE bookId = :bookId")
    suspend fun deleteSearchIndexByBookId(bookId: Long)

    @Query("DELETE FROM search_index")
    suspend fun deleteAllSearchIndex()
}
