package com.haikal.todo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haikal.todo.data.Todo
import com.haikal.todo.ui.TodoTheme
import com.haikal.todo.viewmodel.TodoViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: TodoViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            // STATE UNTUK KUSTOMISASI WARNA (GELAP/TERANG)
            val systemTheme = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(systemTheme) }

            TodoTheme(darkTheme = isDarkMode) {
                val todos by viewModel.todos.collectAsState()
                var showDialog by remember { mutableStateOf(false) }

                // STATE UNTUK WAKTU REAL-TIME
                var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
                
                // Coroutine untuk membuat jam berdetak setiap 1 detik
                LaunchedEffect(Unit) {
                    while (true) {
                        delay(1000)
                        currentTime = System.currentTimeMillis()
                    }
                }

                // Format Tanggal dan Waktu
                val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))
                val dateString = dateFormat.format(Date(currentTime))
                val timeString = timeFormat.format(Date(currentTime))

                // Mendapatkan Info Ponsel (Merek dan Tipe)
                val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                val model = Build.MODEL
                val deviceInfo = "$manufacturer $model"

                val completedCount = todos.count { it.isDone }
                val progress by animateFloatAsState(
                    targetValue = if (todos.isEmpty()) 0f else completedCount.toFloat() / todos.size,
                    animationSpec = tween(durationMillis = 500),
                    label = "ProgressAnimation"
                )

                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                title = { Text("Daftar Tugas") },
                                actions = {
                                    // TOMBOL KUSTOMISASI WARNA (TOGGLE TEMA)
                                    IconButton(onClick = { isDarkMode = !isDarkMode }) {
                                        Icon(
                                            imageVector = if (isDarkMode) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                                            contentDescription = "Ganti Tema"
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(bottomEnd = 8.dp, bottomStart = 8.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showDialog = true },
                            shape = RoundedCornerShape(24.dp),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Tambah Tugas")
                        }
                    }
                ) { paddingValues ->
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // KARTU DASHBOARD INFORMASI (Waktu & Device)
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = timeString, 
                                    fontSize = 36.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateString, 
                                    fontSize = 16.sp, 
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "📱 Ponsel: $deviceInfo", 
                                        fontSize = 14.sp, 
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        // DAFTAR TUGAS
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(todos, key = { it.id }) { todo ->
                                TodoItemCard(
                                    todo = todo,
                                    onCheckedChange = { viewModel.toggleTodo(todo) },
                                    onDelete = { viewModel.deleteTodo(todo) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) } // Jarak untuk FAB
                        }
                    }

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

// ... (Biarkan fungsi TodoItemCard dan AddTodoDialog persis seperti sebelumnya) ...

@Composable
fun TodoItemCard(todo: Todo, onCheckedChange: () -> Unit, onDelete: () -> Unit) {
    val cardColor by animateColorAsState(
        targetValue = if (todo.isDone) 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else 
            MaterialTheme.colorScheme.secondaryContainer,
        animationSpec = tween(durationMillis = 300),
        label = "CardColorAnimation"
    )

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isDone, 
                onCheckedChange = { onCheckedChange() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (todo.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.onSecondaryContainer
                )
                if (todo.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (todo.isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete, 
                    contentDescription = "Hapus", 
                    tint = if (todo.isDone) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                           else MaterialTheme.colorScheme.error
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
        shape = RoundedCornerShape(28.dp),
        title = { Text("Tugas Baru") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Apa yang ingin dikerjakan?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detail tambahan (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, description) },
                shape = RoundedCornerShape(20.dp)
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
