package com.haikal.todo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Mendefinisikan tabel apa saja yang ada di database ini
@Database(entities = [Todo::class], version = 1, exportSchema = false)
abstract class TodoDatabase : RoomDatabase() {
    
    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var Instance: TodoDatabase? = null

        // Mencegah pembuatan multiple instance database secara bersamaan
        fun getDatabase(context: Context): TodoDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TodoDatabase::class.java,
                    "todo_database"
                ).build().also { Instance = it }
            }
        }
    }
}
