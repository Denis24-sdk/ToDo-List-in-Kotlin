package com.example.myfirstapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.example.myfirstapp.ui.theme.BlueColors
import com.example.myfirstapp.ui.theme.BrownColors
import com.example.myfirstapp.ui.theme.GreenColors
import com.example.myfirstapp.ui.theme.MyFirstAppTheme
import com.example.myfirstapp.ui.theme.OrangeColors
import com.example.myfirstapp.ui.theme.PurpleColors
import com.example.myfirstapp.ui.theme.RedColors
import com.example.myfirstapp.ui.theme.TealColors
import com.example.myfirstapp.ui.theme.UniversalDarkColors
import com.example.myfirstapp.ui.theme.YellowColors
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// --- Data ---
data class Task(
    val id: Int,
    val text: String,
    val done: Boolean,
    val subtasks: List<Task> = emptyList()
) {
    fun safeCopy(
        id: Int = this.id,
        text: String = this.text,
        done: Boolean = this.done,
        subtasks: List<Task>? = this.subtasks
    ): Task {
        return copy(
            id = id,
            text = text,
            done = done,
            subtasks = subtasks ?: emptyList()
        )
    }
}
data class TaskList(val id: Int, val name: String, val tasks: List<Task>)
enum class TaskFilter { ALL, ACTIVE, DONE }
enum class SortOption(val displayName: String) {
    A_TO_Z("А-Я"),
    Z_TO_A("Я-А"),
    NEWEST_FIRST("Сначала новые"),
    OLDEST_FIRST("Сначала старые"),
    UNCOMPLETED_FIRST("Сначала невыполненные")
}
// --- DataStore ---
val Context.dataStore by preferencesDataStore(name = "tasks_datastore")
val TASK_LISTS_KEY = stringPreferencesKey("task_lists_json")
val ACTIVE_THEME_INDEX_KEY = intPreferencesKey("active_theme_index")
val ACTIVE_LIST_ID_KEY = intPreferencesKey("active_list_id")
val gson = Gson()
suspend fun Context.saveTaskLists(lists: List<TaskList>) {
    val json = gson.toJson(lists)
    dataStore.edit { prefs -> prefs[TASK_LISTS_KEY] = json }
}
fun Context.loadTaskLists(): Flow<List<TaskList>> = dataStore.data
    .map { prefs ->
        val json = prefs[TASK_LISTS_KEY] ?: "[]"
        val type = object : TypeToken<List<TaskList>>() {}.type
        val lists: List<TaskList> = gson.fromJson(json, type)
        lists.map { taskList -> taskList.copy(tasks = fixTasks(taskList.tasks)) }
    }
suspend fun Context.saveActiveThemeIndex(index: Int) {
    dataStore.edit { prefs -> prefs[ACTIVE_THEME_INDEX_KEY] = index }
}
fun Context.readActiveThemeIndex(): Flow<Int> = dataStore.data
    .map { prefs -> prefs[ACTIVE_THEME_INDEX_KEY] ?: 0 }
suspend fun Context.saveActiveListId(id: Int) {
    dataStore.edit { prefs -> prefs[ACTIVE_LIST_ID_KEY] = id }
}
fun Context.readActiveListId(): Flow<Int> = dataStore.data
    .map { prefs -> prefs[ACTIVE_LIST_ID_KEY] ?: -1 }

// --- Helpers ---
fun fixTasks(tasks: List<Task>?): List<Task> {
    if (tasks == null) return emptyList()
    return tasks.map { task -> task.safeCopy(subtasks = fixTasks(task.subtasks)) }
}

fun updateTaskInList(tasks: List<Task>, updatedTask: Task): List<Task> {
    return tasks.map { task ->
        if (task.id == updatedTask.id) updatedTask.safeCopy()
        else task.safeCopy(subtasks = updateTaskInList(task.subtasks, updatedTask))
    }
}
fun deleteTaskFromList(tasks: List<Task>, taskId: Int): List<Task> {
    return tasks.filter { it.id != taskId }
        .map { it.safeCopy(subtasks = deleteTaskFromList(it.subtasks, taskId)) }
}
fun collectAllIds(task: Task): List<Int> {
    return listOf(task.id) + task.subtasks.flatMap { collectAllIds(it) }
}

fun propagateTaskDoneState(tasks: List<Task>): List<Task> {
    return tasks.map { task ->
        val updatedSubtasks = propagateTaskDoneState(task.subtasks)
        val allSubtasksDone = updatedSubtasks.isNotEmpty() && updatedSubtasks.all { it.done }
        val doneNew = if (updatedSubtasks.isEmpty()) task.done else allSubtasksDone
        task.safeCopy(done = doneNew, subtasks = updatedSubtasks)
    }
}

// --- MainActivity ---
class MainActivity : ComponentActivity() {
    private val themeList = listOf(
        BlueColors, UniversalDarkColors, RedColors, GreenColors,
        YellowColors, OrangeColors, PurpleColors, TealColors, BrownColors
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listsState = mutableStateOf<List<TaskList>>(emptyList())
        val currentTheme = mutableStateOf(themeList[0])
        val activeListIdState = mutableIntStateOf(-1)
        lifecycleScope.launch {
            val loadedLists = applicationContext.loadTaskLists().firstOrNull() ?: emptyList()
            val themeIndex = applicationContext.readActiveThemeIndex().firstOrNull() ?: 0
            val savedActiveListId = applicationContext.readActiveListId().firstOrNull() ?: -1
            val listsToUse = if (loadedLists.isNotEmpty()) loadedLists else listOf(
                TaskList(1, "Мои задачи", listOf(
                    Task(1, "Купить хлеб", false),
                    Task(2, "Позвонить маме", true),
                    Task(3, "Сделать домашку", false)
                ))
            )
            listsState.value = listsToUse
            activeListIdState.intValue = if (listsToUse.any { it.id == savedActiveListId })
                savedActiveListId else listsToUse.firstOrNull()?.id ?: -1
            currentTheme.value = themeList.getOrElse(themeIndex) { themeList[0] }
        }
        setContent {
            MyFirstAppTheme(colorScheme = currentTheme.value) {
                ToDoAppScreen(
                    lists = listsState.value,
                    onListsChange = { newLists ->
                        listsState.value = newLists
                        lifecycleScope.launch { applicationContext.saveTaskLists(newLists) }
                    },
                    currentTheme = currentTheme.value,
                    onThemeChange = { newTheme ->
                        val newIndex = themeList.indexOf(newTheme).takeIf { it >= 0 } ?: 0
                        currentTheme.value = newTheme
                        lifecycleScope.launch { applicationContext.saveActiveThemeIndex(newIndex) }
                    },
                    activeListId = activeListIdState.intValue,
                    onActiveListIdChange = { newActiveListId ->
                        activeListIdState.intValue = newActiveListId
                        lifecycleScope.launch { applicationContext.saveActiveListId(newActiveListId) }
                    }
                )
            }
        }
    }
}

// --- UI ---
@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Task) -> Unit,
    onDeleteRequest: (Task) -> Unit,
    onAddSubtask: (Task, String) -> Unit,
    onTextChange: (Task) -> Unit,
    taskIdPendingDelete: Int?,
    onDeleteIconClick: (Task) -> Unit,
    editingTaskId: Int?,
    onEditingTaskChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    level: Int = 0,
    isLast: Boolean = true,
    hasNextSiblingAtLevel: List<Boolean> = emptyList(),
    leftIndents: List<Dp> = listOf(0.dp, 15.9.dp, 26.7.dp)
) {
    var editText by remember { mutableStateOf(TextFieldValue(task.text)) }
    var showAddSubtaskField by remember { mutableStateOf(false) }
    var subtaskInput by remember { mutableStateOf(TextFieldValue("")) }
    val isEditing = (editingTaskId == task.id)
    val lineColor = MaterialTheme.colorScheme.primary
    val lineStrokeWidth = 5f
    val itemHeight = 48.dp
    val indent = if (level < leftIndents.size) leftIndents[level] else leftIndents.last()
    Column(modifier = modifier.padding(start = indent)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(itemHeight).fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.width(if (level == 0) 0.dp else leftIndents.getOrElse(level) { 0.dp }).fillMaxHeight()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val heightPx = size.height
                    val halfHeight = heightPx / 2f
                    val indentPx = indent.toPx()
                    if (level == 1) {
                        val x = indentPx / 2f
                        drawLine(color = lineColor, strokeWidth = lineStrokeWidth,
                            start = Offset(x, halfHeight),
                            end = Offset(indentPx + 37f, halfHeight),
                            cap = StrokeCap.Round)
                        drawLine(color = lineColor, strokeWidth = lineStrokeWidth,
                            start = Offset(x, if (isLast) -35f else -35f),
                            end = Offset(x, if (isLast) halfHeight else heightPx),
                            cap = StrokeCap.Round)
                    }
                }
            }
            Checkbox(checked = task.done, onCheckedChange = { checked ->
                fun setDoneRec(task: Task, done: Boolean): Task {
                    return task.safeCopy(done = done, subtasks = task.subtasks.map { setDoneRec(it, done) })
                }
                val updatedTask = setDoneRec(task, checked)
                onCheckedChange(updatedTask)
            })
            if (isEditing) {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { newValue -> editText = newValue },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                    trailingIcon = {
                        Row {
                            IconButton(onClick = {
                                val newText = editText.text.trim()
                                if (newText.isNotBlank() && newText != task.text) {
                                    onTextChange(task.copy(text = newText))
                                }
                                onEditingTaskChange(null)
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Сохранить")
                            }
                            IconButton(onClick = {
                                onEditingTaskChange(null)
                                editText = TextFieldValue(task.text)
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Отмена")
                            }
                        }
                    })
            } else {
                Text(text = task.text,
                    modifier = Modifier.weight(1f).clickable {
                        onEditingTaskChange(task.id)
                        editText = TextFieldValue(task.text)
                    },
                    style = if (level == 0)
                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 19.sp)
                    else
                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal)
                )
            }
            if (level == 0) {
                IconButton(onClick = { showAddSubtaskField = !showAddSubtaskField }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить подзадачу")
                }
            }
            IconButton(onClick = { onDeleteIconClick(task) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить задачу",
                    tint = if (taskIdPendingDelete == task.id) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showAddSubtaskField) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 32.dp + indent, bottom = 8.dp)) {
                OutlinedTextField(
                    value = subtaskInput,
                    onValueChange = { subtaskInput = it },
                    placeholder = {
                        Text("Новая подзадача", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    },
                    modifier = Modifier.weight(1f).height(54.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    singleLine = true,
                )
                IconButton(onClick = {
                    if (subtaskInput.text.isNotBlank()) {
                        onAddSubtask(task, subtaskInput.text.trim())
                        subtaskInput = TextFieldValue("")
                        showAddSubtaskField = false
                    }
                }) {
                    Icon(Icons.Default.Check, contentDescription = "Подтвердить")
                }
                IconButton(onClick = {
                    showAddSubtaskField = false
                    subtaskInput = TextFieldValue("")
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Отмена")
                }
            }
        }
        val childCount = task.subtasks.size
        task.subtasks.forEachIndexed { index, subtask ->
            val hasNextSibling = index < childCount - 1
            TaskItem(
                task = subtask,
                onCheckedChange = onCheckedChange,
                onDeleteRequest = onDeleteRequest,
                onAddSubtask = onAddSubtask,
                onTextChange = onTextChange,
                taskIdPendingDelete = taskIdPendingDelete,
                onDeleteIconClick = onDeleteIconClick,
                editingTaskId = editingTaskId,
                onEditingTaskChange = onEditingTaskChange,
                level = level + 1,
                isLast = !hasNextSibling,
                hasNextSiblingAtLevel = hasNextSiblingAtLevel + hasNextSibling,
                leftIndents = leftIndents
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ToDoAppScreen(
    lists: List<TaskList>,
    onListsChange: (List<TaskList>) -> Unit,
    currentTheme: ColorScheme,
    onThemeChange: (ColorScheme) -> Unit,
    activeListId: Int,
    onActiveListIdChange: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var editingTaskId by remember { mutableStateOf<Int?>(null) }
    var showDeleteDoneDialog by remember { mutableStateOf(false) }
    var showAddListDialog by remember { mutableStateOf(false) }
    var showDeleteListDialog by remember { mutableStateOf(false) }
    var deleteListId by remember { mutableStateOf<Int?>(null) }
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var filter by remember { mutableStateOf(TaskFilter.ALL) }
    var newListName by remember { mutableStateOf(TextFieldValue("")) }
    var selectedSortOption by remember { mutableStateOf(SortOption.A_TO_Z) }
    var sortExpanded by remember { mutableStateOf(false) }
    var taskIdPendingDelete by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(taskIdPendingDelete) {
        if (taskIdPendingDelete != null) {
            delay(2000)
            taskIdPendingDelete = null
        }
    }

    val activeList = lists.find { it.id == activeListId }

    LaunchedEffect(lists) {
        if (lists.none { it.id == activeListId }) {
            val newActiveId = lists.firstOrNull()?.id ?: -1
            onActiveListIdChange(newActiveId)
            editingTaskId = null
        }
    }

    fun addTask(text: String) {
        if (text.isBlank() || activeList == null) return
        val currentTasks = activeList.tasks
        val currentIds = currentTasks.flatMap { collectAllIds(it) }.toSet()
        var newId = 1
        while (newId in currentIds) newId++
        val newTask = Task(newId, text.trim(), false)
        val updatedList = activeList.copy(tasks = currentTasks + newTask)
        val updatedLists = lists.map { if (it.id == activeListId) updatedList else it }
        onListsChange(updatedLists)
        input = TextFieldValue("")
        focusManager.clearFocus()
        keyboardController?.hide()
        editingTaskId = null
    }

    fun updateTask(task: Task) {
        val list = activeList ?: return
        val updatedTasks = updateTaskInList(list.tasks, task)
        val propagatedTasks = propagateTaskDoneState(updatedTasks)
        val updatedList = list.copy(tasks = propagatedTasks)
        val updatedLists = lists.map { if (it.id == activeListId) updatedList else it }
        onListsChange(updatedLists)
        editingTaskId = null
    }

    fun deleteTask(task: Task) {
        val list = activeList ?: return
        val updatedTasks = deleteTaskFromList(list.tasks, task.id)
        val updatedList = list.copy(tasks = updatedTasks)
        val updatedLists = lists.map { if (it.id == activeListId) updatedList else it }
        onListsChange(updatedLists)
        taskIdPendingDelete = null
        editingTaskId = null
    }

    fun addSubtask(parentTask: Task, text: String) {
        val list = activeList ?: return
        val currentIds = list.tasks.flatMap { collectAllIds(it) }.toSet()
        var newId = 1
        while (newId in currentIds) newId++
        val newSubtask = Task(newId, text, false)
        fun addSubtaskRec(tasks: List<Task>): List<Task> = tasks.map { task ->
            if (task.id == parentTask.id) {
                task.safeCopy(subtasks = task.subtasks + newSubtask)
            } else {
                task.safeCopy(subtasks = addSubtaskRec(task.subtasks))
            }
        }
        val updatedTasks = addSubtaskRec(list.tasks)
        val updatedList = list.copy(tasks = updatedTasks)
        val updatedLists = lists.map { if (it.id == activeListId) updatedList else it }
        onListsChange(updatedLists)
        editingTaskId = null
    }

    fun deleteDoneTasks() {
        val list = activeList ?: return
        // Удаляем только top-level задачи, у которых done == true
        val filteredTasks = list.tasks.filter { !it.done }
        val updatedList = list.copy(tasks = filteredTasks)
        val updatedLists = lists.map { if (it.id == activeListId) updatedList else it }
        onListsChange(updatedLists)
        editingTaskId = null
    }

    fun addTaskList(name: String) {
        if (name.isBlank()) return
        val newId = (lists.maxOfOrNull { it.id } ?: 0) + 1
        val newList = TaskList(newId, name.trim(), emptyList())
        val updatedLists = lists + newList
        onListsChange(updatedLists)
        onActiveListIdChange(newId)
        editingTaskId = null
    }

    fun deleteTaskListConfirmed(id: Int) {
        val updatedLists = lists.filter { it.id != id }
        onListsChange(updatedLists)
        if (activeListId == id) {
            onActiveListIdChange(updatedLists.firstOrNull()?.id ?: -1)
        }
        editingTaskId = null
    }

    fun deleteTaskListRequest(id: Int) {
        deleteListId = id
        showDeleteListDialog = true
    }

    fun filteredSortedTasks(): List<Task> {
        if (activeList == null) return emptyList()
        val filtered = when (filter) {
            TaskFilter.ALL -> activeList.tasks
            TaskFilter.ACTIVE -> activeList.tasks.filter { !it.done }
            TaskFilter.DONE -> activeList.tasks.filter { it.done }
        }
        return when (selectedSortOption) {
            SortOption.A_TO_Z -> filtered.sortedBy { it.text.lowercase() }
            SortOption.Z_TO_A -> filtered.sortedByDescending { it.text.lowercase() }
            SortOption.NEWEST_FIRST -> filtered.sortedByDescending { it.id }
            SortOption.OLDEST_FIRST -> filtered.sortedBy { it.id }
            SortOption.UNCOMPLETED_FIRST -> filtered.sortedWith(
                compareBy<Task> { it.done }.thenBy { it.text.lowercase() }
            )
        }
    }

    fun onDeleteIconClick(task: Task) {
        if (taskIdPendingDelete == task.id) {
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                deleteTask(task)
            }
        } else {
            taskIdPendingDelete = task.id
            scope.launch { snackbarHostState.showSnackbar("Нажмите повторно для удаления") }
        }
    }

    // Для кнопки info в меню
    fun showMenuInfo() {
        scope.launch {
            snackbarHostState.showSnackbar("Свайпните для открытия или закрытия меню")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight(0.9f)
                    .padding(vertical = 8.dp)
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Ваши списки",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        IconButton(onClick = { showMenuInfo() }) {
                            Icon(Icons.Default.Info, contentDescription = "Информация", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(lists, key = { it.id }) { list ->
                            val selected = list.id == activeListId
                            ListItem(
                                headlineContent = { Text(list.name) },
                                leadingContent = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                                trailingContent = {
                                    if (lists.size > 1) {
                                        IconButton(
                                            onClick = { deleteTaskListRequest(list.id) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Удалить список",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onActiveListIdChange(list.id)
                                        editingTaskId = null
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 8.dp)
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surface
                                    )
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
                                        editingTaskId = null
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Выберите тему",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        val themeList = listOf(
                            BlueColors, UniversalDarkColors, RedColors,
                            GreenColors, YellowColors, OrangeColors,
                            PurpleColors, TealColors, BrownColors
                        )
                        val themeNames = listOf(
                            "Синяя", "Тёмная", "Красная",
                            "Зелёная", "Жёлтая", "Оранжевая",
                            "Фиолетовая", "Бирюзовая", "Коричневая"
                        )
                        itemsIndexed(themeList) { index, theme ->
                            val isSelected = theme == currentTheme
                            Surface(
                                tonalElevation = if (isSelected) 8.dp else 0.dp,
                                shadowElevation = if (isSelected) 8.dp else 0.dp,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.width(120.dp).clickable {
                                    onThemeChange(theme)
                                    scope.launch { drawerState.close() }
                                }
                            ) {
                                Column(
                                    Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = themeNames[index],
                                        color = theme.primary,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier.size(width = 60.dp, height = 30.dp)
                                            .background(color = theme.primary, shape = MaterialTheme.shapes.small)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        gesturesEnabled = true,
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(activeList?.name ?: "Нет списков") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open() else drawerState.close()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    },
                    actions = {
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
                    modifier = Modifier.padding(innerPadding).fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                    ) {
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Введите задачу", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge,
                            trailingIcon = {
                                IconButton(
                                    onClick = { addTask(input.text) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        tonalElevation = 4.dp,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                            Icon(Icons.Default.Add, contentDescription = "Добавить", tint = MaterialTheme.colorScheme.onPrimary)
                                        }
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    addTask(input.text)
                                }
                            )
                        )

                        Spacer(Modifier.height(4.dp))

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
                        }
                        Spacer(Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Сортировка:", modifier = Modifier.padding(end = 8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { sortExpanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(selectedSortOption.displayName, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = sortExpanded,
                                    onDismissRequest = { sortExpanded = false }
                                ) {
                                    SortOption.entries.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.displayName) },
                                            onClick = {
                                                selectedSortOption = option
                                                sortExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))

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
                            Spacer(Modifier.height(4.dp))
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
                                items(items = displayedTasks, key = { it.id }) { task ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                                        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                                    ) {
                                        TaskItem(
                                            task = task,
                                            onCheckedChange = { updatedTask -> updateTask(updatedTask) },
                                            onDeleteRequest = { deleteTask(it) },
                                            onAddSubtask = { parent, text -> addSubtask(parent, text) },
                                            onTextChange = { updatedTask -> updateTask(updatedTask) },
                                            taskIdPendingDelete = taskIdPendingDelete,
                                            onDeleteIconClick = { onDeleteIconClick(it) },
                                            editingTaskId = editingTaskId,
                                            onEditingTaskChange = { newId -> editingTaskId = newId },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
    // Диалоги
    if (showDeleteDoneDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDoneDialog = false },
            title = { Text("Подтвердите удаление") },
            text = { Text("Вы уверены, что хотите удалить все выполненные задачи?") },
            confirmButton = {
                TextButton(onClick = {
                    deleteDoneTasks()
                    showDeleteDoneDialog = false
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDoneDialog = false }) { Text("Отмена") } }
        )
    }
    if (showDeleteListDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteListDialog = false
                deleteListId = null
            },
            title = { Text("Подтвердите удаление списка") },
            text = { Text("Вы уверены, что хотите удалить этот список задач? Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteListId?.let { deleteTaskListConfirmed(it) }
                    showDeleteListDialog = false
                    deleteListId = null
                }) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteListDialog = false
                    deleteListId = null
                }) { Text("Отмена") }
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
                    placeholder = { Text("Новый список", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) },
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
                ) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddListDialog = false
                        newListName = TextFieldValue("")
                    }
                ) { Text("Отмена") }
            }
        )
    }
}