package com.haikal.todo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haikal.todo.data.Todo
import com.haikal.todo.data.TodoDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dao = TodoDatabase.getDatabase(application).todoDao()

    // Membaca data secara reaktif (Flow). UI akan otomatis update jika ada perubahan di database.
    val todos: StateFlow<List<Todo>> = dao.getAllTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTodo(title: String, description: String) {
        viewModelScope.launch { 
            dao.insertTodo(Todo(title = title, description = description)) 
        }
    }

    fun toggleTodo(todo: Todo) {
        // Membalikkan status isDone (true jadi false, false jadi true)
        viewModelScope.launch { 
            dao.updateTodo(todo.copy(isDone = !todo.isDone)) 
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch { 
            dao.deleteTodo(todo) 
        }
    }
}
