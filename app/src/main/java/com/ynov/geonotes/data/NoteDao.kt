package com.ynov.geonotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY date DESC")
    fun getAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getById(id: Long): Flow<Note?>

    @Insert
    suspend fun insert(note: Note): Long

    @Delete
    suspend fun delete(note: Note)
}
