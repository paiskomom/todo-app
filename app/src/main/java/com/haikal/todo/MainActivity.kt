package com.haikal.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.haikal.todo.data.Todo
import com.haikal.todo.ui.TodoTheme
import com.haikal.todo.viewmodel.TodoViewModel

class MainActivity : ComponentActivity() {
    // Inisialisasi ViewModel
    private val viewModel: TodoViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TodoTheme {
                val todos by viewModel.todos.collectAsState()
                var showDialog by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Daftar Tugas") },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showDialog = true },
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Tambah Tugas")
                        }
                    }
                ) { paddingValues ->
                    
                    // LazyColumn adalah RecyclerView versi Jetpack Compose
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(todos) { todo ->
                            TodoItemCard(
                                todo = todo,
                                onCheckedChange = { viewModel.toggleTodo(todo) },
                                onDelete = { viewModel.deleteTodo(todo) }
                            )
                        }
                    }

                    // Menampilkan Pop-up dialog jika tombol tambah ditekan
                    if (showDialog) {
                        AddTodoDialog(
                            onDismiss = { showDialog = false },
                            onConfirm = { title, desc ->
                                viewModel.addTodo(title, desc)
                                showDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodoItemCard(todo: Todo, onCheckedChange: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isDone, 
                onCheckedChange = { onCheckedChange() }
            )
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    // Mencoret teks jika tugas sudah selesai
                    textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (todo.isDone) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
                if (todo.description.isNotEmpty()) {
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete, 
                    contentDescription = "Hapus Tugas", 
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AddTodoDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tugas Baru") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Apa yang ingin dikerjakan?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detail tambahan (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (title.isNotBlank()) onConfirm(title, description) 
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
