package com.example.jetpackapploginmvvm.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.jetpackapploginmvvm.model.Mascota
import com.example.jetpackapploginmvvm.model.User

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): User?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertMascota(mascota: Mascota)

    @Query("SELECT * FROM mascotas WHERE ownerUsername = :username LIMIT 1")
    suspend fun getMascota(username: String): Mascota?
}