package com.example.myfirstapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import android.content.Context

val Context.dataStore by preferencesDataStore(name = "tasks_prefs")
val TASK_LISTS_KEY = stringPreferencesKey("task_lists_key")

@Serializable
data class Task(val id: Int, val text: String, val done: Boolean)

@Serializable
data class TaskList(val id: Int, val name: String, val tasks: List<Task>)

enum class TaskFilter { ALL, ACTIVE, DONE }

val jsonFormat = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}

fun getTaskListsFlow(context: Context): Flow<List<TaskList>> = context.dataStore.data
    .map { prefs ->
        val json = prefs[TASK_LISTS_KEY] ?: "[]"
        try {
            jsonFormat.decodeFromString(json)
        } catch (e: Exception) {
            emptyList<TaskList>()
        }
    }

suspend fun saveTaskLists(context: Context, lists: List<TaskList>) {
    val json = jsonFormat.encodeToString(lists)
    context.dataStore.edit { prefs ->
        prefs[TASK_LISTS_KEY] = json
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ToDoAppScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ToDoAppScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val taskListsFlow = remember { getTaskListsFlow(context) }
    val taskLists by taskListsFlow.collectAsState(initial = emptyList())

    var lists by remember { mutableStateOf(taskLists) }
    LaunchedEffect(taskLists) {
        lists = taskLists.ifEmpty {
            listOf(TaskList(1, "Мои задачи", emptyList()))
        }
    }

    var activeListId by remember { mutableStateOf(lists.firstOrNull()?.id ?: 1) }
    val activeList = lists.find { it.id == activeListId } ?: lists.firstOrNull()

    var input by remember { mutableStateOf(TextFieldValue("")) }
    var filter by remember { mutableStateOf(TaskFilter.ALL) }
    var sortAsc by remember { mutableStateOf(true) }
    var showDeleteDoneDialog by remember { mutableStateOf(false) }
    var showAddListDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf(TextFieldValue("")) }

    fun save(allLists: List<TaskList>) {
        allLists.forEach { list ->
            val ids = list.tasks.map { it.id }
            if (ids.size != ids.toSet().size) {
                Log.e("ToDoApp", "Дубликаты ID задач в списке '${list.name}': $ids")
            }
        }
        scope.launch {
            saveTaskLists(context, allLists)
        }
    }

    fun addTask(text: String) {
        if (text.isBlank() || activeList == null) return
        val currentTasks = activeList.tasks
        val currentIds = currentTasks.map { it.id }.toSet()
        var newId = 1
        while (newId in currentIds) newId++
        val newTask = Task(newId, text.trim(), false)
        val updatedList = activeList.copy(tasks = currentTasks + newTask)
        lists = lists.map { if (it.id == activeListId) updatedList else it }
        save(lists)
        input = TextFieldValue("")
    }

    fun updateTask(updatedTask: Task) {
        if (activeList == null) return
        val updatedTasks = activeList.tasks.map { if (it.id == updatedTask.id) updatedTask else it }
        val updatedList = activeList.copy(tasks = updatedTasks)
        lists = lists.map { if (it.id == activeListId) updatedList else it }
        save(lists)
    }

    fun deleteTask(taskToDelete: Task) {
        if (activeList == null) return
        val updatedTasks = activeList.tasks.filter { it.id != taskToDelete.id }
        val updatedList = activeList.copy(tasks = updatedTasks)
        lists = lists.map { if (it.id == activeListId) updatedList else it }
        save(lists)
    }

    fun deleteDoneTasks() {
        if (activeList == null) return
        val updatedTasks = activeList.tasks.filter { !it.done }
        val updatedList = activeList.copy(tasks = updatedTasks)
        lists = lists.map { if (it.id == activeListId) updatedList else it }
        save(lists)
    }

    fun addTaskList(name: String) {
        if (name.isBlank()) return
        val currentIds = lists.map { it.id }.toSet()
        var newId = 1
        while (newId in currentIds) newId++
        val newList = TaskList(newId, name.trim(), emptyList())
        lists = lists + newList
        activeListId = newId
        save(lists)
    }

    fun deleteTaskList(listId: Int) {
        if (lists.size <= 1) return
        lists = lists.filter { it.id != listId }
        if (activeListId == listId) {
            activeListId = lists.firstOrNull()?.id ?: 1
        }
        save(lists)
    }

    fun filteredSortedTasks(): List<Task> {
        if (activeList == null) return emptyList()
        val filtered = when (filter) {
            TaskFilter.ALL -> activeList.tasks
            TaskFilter.ACTIVE -> activeList.tasks.filter { !it.done }
            TaskFilter.DONE -> activeList.tasks.filter { it.done }
        }
        return if (sortAsc) filtered.sortedBy { it.text.lowercase() }
        else filtered.sortedByDescending { it.text.lowercase() }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ToDo-лист: ")
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(activeList?.name ?: "Нет списков", style = MaterialTheme.typography.headlineSmall)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                lists.forEach { list ->
                                    DropdownMenuItem(
                                        text = { Text(list.name) },
                                        onClick = {
                                            activeListId = list.id
                                            expanded = false
                                        },
                                        trailingIcon = {
                                            if (lists.size > 1) {
                                                IconButton(
                                                    onClick = {
                                                        deleteTaskList(list.id)
                                                        expanded = false
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Удалить список")
                                                }
                                            }
                                        }
                                    )
                                }
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("Добавить список") },
                                    onClick = {
                                        expanded = false
                                        showAddListDialog = true
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { addTask(input.text) },
                icon = { Icon(Icons.Default.Add, contentDescription = "Добавить задачу") },
                text = { Text("Добавить") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .imePadding()
                    .navigationBarsPadding()
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Например: купить хлеб") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    trailingIcon = {
                        IconButton(
                            onClick = { addTask(input.text) }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить")
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filter == TaskFilter.ALL,
                        onClick = { filter = TaskFilter.ALL },
                        label = { Text("Все") },
                        leadingIcon = { Icon(Icons.Default.List, contentDescription = null) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = filter == TaskFilter.ACTIVE,
                        onClick = { filter = TaskFilter.ACTIVE },
                        label = { Text("Активные") },
                        leadingIcon = { Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    FilterChip(
                        selected = filter == TaskFilter.DONE,
                        onClick = { filter = TaskFilter.DONE },
                        label = { Text("Выполненные") },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                    )

                    Spacer(Modifier.weight(1f))

                    IconButton(onClick = { sortAsc = !sortAsc }) {
                        Icon(
                            imageVector = Icons.Default.SortByAlpha,
                            contentDescription = "Сортировка"
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (activeList == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Список пуст",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    return@Column
                }

                if (activeList.tasks.any { it.done }) {
                    Button(
                        onClick = { showDeleteDoneDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить выполненные")
                        Spacer(Modifier.width(8.dp))
                        Text("Удалить все выполненные")
                    }
                    Spacer(Modifier.height(16.dp))
                }

                val displayedTasks = filteredSortedTasks()

                if (displayedTasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Список задач пуст",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = displayedTasks,
                            key = { it.id }
                        ) { task ->
                            AnimatedVisibility(
                                visible = true,
                                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                            ) {
                                TaskCardModern(
                                    task = task,
                                    onCheckedChange = { done -> updateTask(task.copy(done = done)) },
                                    onDelete = { deleteTask(task) }
                                )
                            }
                        }
                    }
                }
            }
        }
    )

    if (showDeleteDoneDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDoneDialog = false },
            title = { Text("Подтвердите удаление") },
            text = { Text("Вы уверены, что хотите удалить все выполненные задачи?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteDoneTasks()
                        showDeleteDoneDialog = false
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDoneDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showAddListDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddListDialog = false
                newListName = TextFieldValue("")
            },
            title = { Text("Новый список задач") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Название списка") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        addTaskList(newListName.text)
                        showAddListDialog = false
                        newListName = TextFieldValue("")
                    },
                    enabled = newListName.text.isNotBlank()
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddListDialog = false
                        newListName = TextFieldValue("")
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun TaskCardModern(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (task.done) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.done,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = task.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (task.done) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

