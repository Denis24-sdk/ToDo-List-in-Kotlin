package com.example.myfirstapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// --- Data ---

data class Task(val id: Int, val text: String, val done: Boolean)
data class TaskList(val id: Int, val name: String, val tasks: List<Task>)

enum class TaskFilter { ALL, ACTIVE, DONE }

// --- DataStore ---

// Ключ для сохранения списков задач в DataStore
val Context.dataStore by preferencesDataStore(name = "tasks_datastore")
val TASK_LISTS_KEY = stringPreferencesKey("task_lists_json")

// Gson для конвертации списков задач в JSON и обратно
val gson = Gson()

suspend fun Context.saveTaskLists(lists: List<TaskList>) {
    val json = gson.toJson(lists)
    dataStore.edit { prefs ->
        prefs[TASK_LISTS_KEY] = json
    }
}

fun Context.loadTaskLists(): Flow<List<TaskList>> = dataStore.data
    .map { prefs ->
        val json = prefs[TASK_LISTS_KEY] ?: "[]"
        val type = object : TypeToken<List<TaskList>>() {}.type
        gson.fromJson(json, type)
    }

// --- MainActivity ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Загружаем списки из DataStore, если данных нет — создаём дефолтный список
        val listsState = mutableStateOf<List<TaskList>>(emptyList())

        lifecycleScope.launch {
            applicationContext.loadTaskLists().collectLatest { loadedLists ->
                if (loadedLists.isNotEmpty()) {
                    listsState.value = loadedLists
                } else {
                    listsState.value = listOf(
                        TaskList(
                            id = 1,
                            name = "Мои задачи",
                            tasks = listOf(
                                Task(1, "Купить хлеб", false),
                                Task(2, "Позвонить маме", true),
                                Task(3, "Сделать домашку", false),
                            )
                        )
                    )
                }
            }
        }

        setContent {
            MaterialTheme {
                ToDoAppScreen(
                    lists = listsState.value,
                    onListsChange = { newLists ->
                        listsState.value = newLists
                        lifecycleScope.launch {
                            applicationContext.saveTaskLists(newLists)
                        }
                    }
                )
            }
        }
    }
}

// --- UI компоненты ---

// Отображение отдельной задачи с чекбоксом и кнопкой удаления
@Composable
fun TaskCardModern(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Checkbox(
                checked = task.done,
                onCheckedChange = onCheckedChange
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = task.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить задачу")
            }
        }
    }
}

// --- Основной экран ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoAppScreen(
    lists: List<TaskList>,
    onListsChange: (List<TaskList>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var activeListId by remember {
        mutableIntStateOf(lists.firstOrNull()?.id ?: -1)
    }

    LaunchedEffect(lists) {
        if (lists.none { it.id == activeListId }) {
            activeListId = lists.firstOrNull()?.id ?: -1
        }
    }

    val activeList = lists.find { it.id == activeListId }

    var input by remember { mutableStateOf(TextFieldValue("")) }
    var filter by remember { mutableStateOf(TaskFilter.ALL) }
    var sortAsc by remember { mutableStateOf(true) }
    var showDeleteDoneDialog by remember { mutableStateOf(false) }
    var showAddListDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf(TextFieldValue("")) }

    // Основная логика: добавление, обновление, удаление задач и списков

    fun addTask(text: String) {
        if (text.isBlank() || activeList == null) return
        val currentTasks = activeList.tasks
        val currentIds = currentTasks.map { it.id }.toSet()
        var newId = 1
        while (newId in currentIds) newId++
        val newTask = Task(newId, text.trim(), false)
        val updatedList = activeList.copy(tasks = currentTasks + newTask)
        val updatedLists = lists.map { if (it.id == activeListId) updatedList else it }
        onListsChange(updatedLists)
        input = TextFieldValue("")
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    fun updateTask(task: Task) {
        val list = activeList ?: return
        val updatedTasks = list.tasks.map {
            if (it.id == task.id) task else it
        }
        val updatedList = list.copy(tasks = updatedTasks)
        val updatedLists = lists.map { if (it.id == activeListId) updatedList else it }
        onListsChange(updatedLists)
    }

    fun deleteTask(task: Task) {
        val list = activeList ?: return
        val updatedTasks = list.tasks.filter { it.id != task.id }
        val updatedList = list.copy(tasks = updatedTasks)
        val updatedLists = lists.map { if (it.id == activeListId) updatedList else it }
        onListsChange(updatedLists)
    }

    fun deleteDoneTasks() {
        val list = activeList ?: return
        val updatedTasks = list.tasks.filter { !it.done }
        val updatedList = list.copy(tasks = updatedTasks)
        val updatedLists = lists.map { if (it.id == activeListId) updatedList else it }
        onListsChange(updatedLists)
    }

    fun addTaskList(name: String) {
        if (name.isBlank()) return
        val newId = (lists.maxOfOrNull { it.id } ?: 0) + 1
        val newList = TaskList(newId, name.trim(), emptyList())
        val updatedLists = lists + newList
        onListsChange(updatedLists)
        activeListId = newId
    }

    fun deleteTaskList(id: Int) {
        val updatedLists = lists.filter { it.id != id }
        onListsChange(updatedLists)
        if (activeListId == id) {
            activeListId = updatedLists.firstOrNull()?.id ?: -1
        }
    }

    // Фильтрация и сортировка задач в зависимости от выбранных настроек
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

    // --- UI ---

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight(0.9f)
            ) {
                // Боковое меню с перечнем всех списков и кнопкой для создания нового
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ваши списки",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Для открытия списков свайпните вправо")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Подсказка"
                        )
                    }
                }

                LazyColumn {
                    items(lists, key = { it.id }) { list ->
                        ListItem(
                            headlineContent = { Text(list.name) },
                            trailingContent = {
                                if (lists.size > 1) {
                                    IconButton(onClick = {
                                        deleteTaskList(list.id)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить список")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeListId = list.id
                                    scope.launch { drawerState.close() }
                                }
                                .padding(horizontal = 8.dp)
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("Добавить список") },
                            leadingContent = { Icon(Icons.Default.Add, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showAddListDialog = true
                                    scope.launch { drawerState.close() }
                                }
                                .padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        },
        gesturesEnabled = true,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                // Верхний AppBar с кнопкой открытия бокового меню
                CenterAlignedTopAppBar(
                    title = {
                        Text(activeList?.name ?: "Нет списков")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    },
                    // Кнопка подсказки убрана из верхнего бара, чтобы не загромождать интерфейс
                    actions = {
                        // пусто
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.height(16.dp))

                        // Основной экран: ввод новой задачи, фильтры, список задач и кнопка очистки выполненных
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
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .imePadding()
                            .navigationBarsPadding()
                            .align(Alignment.BottomCenter)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = { addTask(input.text) },
                            icon = { Icon(Icons.Default.Add, contentDescription = "Добавить задачу") },
                            text = { Text("Добавить") },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        )
    }

    // Диалог подтверждения удаления всех выполненных задач
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

    // Диалог для добавления нового списка задач
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

