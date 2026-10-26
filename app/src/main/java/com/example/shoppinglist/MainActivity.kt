
package com.example.shoppinglist

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoppinglist.data.ShoppingRepository
import com.example.shoppinglist.data.local.ShoppingDatabase
import com.example.shoppinglist.data.local.ShoppingListEntity
import com.example.shoppinglist.ui.theme.ShoppingListTheme
import com.example.shoppinglist.ui.viewmodel.ShoppingViewModel
import com.example.shoppinglist.ui.viewmodel.ShoppingViewModelFactory
import kotlinx.coroutines.launch

private val Cream = Color(0xFFF7F6F0)
private val DarkGreen = Color(0xFF42664D)
private val SoftGreen = Color(0xFFE6EBDD)
private val TextDark = Color(0xFF28352B)
private val TextMuted = Color(0xFF899087)
private val CardWhite = Color(0xFFFFFEFA)

private val DarkBackground = Color(0xFF171C18)
private val DarkCard = Color(0xFF222923)
private val DarkSoftGreen = Color(0xFF303D32)
private val DarkText = Color(0xFFE7EDE5)
private val DarkMuted = Color(0xFFB0BBB0)
private val DarkAccent = Color(0xFF9BC7A0)

private data class AppPalette(
    val background: Color,
    val card: Color,
    val softCard: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val progressBackground: Color
)

private fun palette(darkTheme: Boolean) = if (darkTheme) {
    AppPalette(
        background = DarkBackground,
        card = DarkCard,
        softCard = DarkSoftGreen,
        text = DarkText,
        muted = DarkMuted,
        accent = DarkAccent,
        progressBackground = Color(0xFF465247)
    )
} else {
    AppPalette(
        background = Cream,
        card = CardWhite,
        softCard = SoftGreen,
        text = TextDark,
        muted = TextMuted,
        accent = DarkGreen,
        progressBackground = Color(0xFFD2DCCB)
    )
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = ShoppingDatabase.getDatabase(applicationContext)
        val repository = ShoppingRepository(
            database.shoppingListDao(),
            database.shoppingProductDao()
        )

        val preferences = getSharedPreferences(
            "shopping_settings",
            Context.MODE_PRIVATE
        )

        setContent {
            val darkTheme = remember {
                mutableStateOf(
                    preferences.getBoolean("dark_theme", false)
                )
            }

            ShoppingListTheme(darkTheme = darkTheme.value) {
                val shoppingViewModel: ShoppingViewModel = viewModel(
                    factory = ShoppingViewModelFactory(repository)
                )

                ShoppingHomeScreen(
                    viewModel = shoppingViewModel,
                    darkTheme = darkTheme.value,
                    onDarkThemeChange = { enabled ->
                        darkTheme.value = enabled
                        preferences.edit()
                            .putBoolean("dark_theme", enabled)
                            .apply()
                    },
                    initialProfileName = preferences.getString(
                        "profile_name",
                        "Пользователь"
                    ) ?: "Пользователь",
                    onProfileNameChange = { name ->
                        preferences.edit()
                            .putString("profile_name", name)
                            .apply()
                    }
                )
            }
        }
    }
}

@Composable
private fun ShoppingHomeScreen(
    viewModel: ShoppingViewModel,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    initialProfileName: String,
    onProfileNameChange: (String) -> Unit
) {
    val colors = palette(darkTheme)
    val lists by viewModel.shoppingLists.collectAsState()
    val completedLists by viewModel.completedLists.collectAsState()

    var openedListId by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    var profileName by remember {
        mutableStateOf(initialProfileName)
    }
    var showEditProfileDialog by remember {
        mutableStateOf(false)
    }
    var editedProfileName by remember {
        mutableStateOf("")
    }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    var editingList by remember {
        mutableStateOf<ShoppingListEntity?>(null)
    }
    var editListName by remember { mutableStateOf("") }
    var editListSubtitle by remember { mutableStateOf("") }

    var deletingList by remember {
        mutableStateOf<ShoppingListEntity?>(null)
    }

    BackHandler(
        enabled = openedListId != null ||
                (selectedTab == 2 && showSettings) ||
                selectedTab != 0
    ) {
        when {
            openedListId != null -> openedListId = null
            selectedTab == 2 && showSettings -> showSettings = false
            selectedTab != 0 -> {
                selectedTab = 0
                showSettings = false
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.createInitialDataIfEmpty()
    }

    val productCounts by produceState(
        initialValue = emptyMap<Int, Pair<Int, Int>>(),
        key1 = lists
    ) {
        value = emptyMap()

        lists.forEach { list ->
            launch {
                viewModel.getProductsForList(list.id).collect { products ->
                    value = value + (
                            list.id to Pair(
                                products.size,
                                products.count { it.bought }
                            )
                            )
                }
            }
        }
    }

    val activeList = lists.firstOrNull { it.id == openedListId }

    if (activeList != null) {
        ShoppingListRoute(
            list = activeList,
            viewModel = viewModel,
            onBack = { openedListId = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        when (selectedTab) {
            0 -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Мои покупки",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )

                    Text(
                        text = "Всё необходимое — в одном месте",
                        fontSize = 14.sp,
                        color = colors.muted
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val totalItems = productCounts.values.sumOf { it.first }
                    val boughtItems = productCounts.values.sumOf { it.second }

                    SummaryCard(
                        listCount = lists.size,
                        totalItems = totalItems,
                        boughtItems = boughtItems,
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Мои списки",
                            modifier = Modifier.weight(1f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )

                        Text(
                            text = "${lists.size} списка",
                            fontSize = 13.sp,
                            color = colors.muted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (lists.isEmpty()) {
                        EmptyListsCard(colors)
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(items = lists, key = { it.id }) { list ->
                                val counts = productCounts[list.id] ?: (0 to 0)

                                ShoppingListCard(
                                    list = list,
                                    totalItems = counts.first,
                                    boughtItems = counts.second,
                                    colors = colors,
                                    onClick = { openedListId = list.id },
                                    onEdit = {
                                        editListName = list.title
                                        editListSubtitle = list.subtitle
                                        editingList = list
                                    },
                                    onDelete = { deletingList = list },
                                    onComplete = { viewModel.completeList(list) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            newListName = ""
                            showCreateDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(17.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent
                        )
                    ) {
                        Text(
                            text = "+  Создать новый список",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            1 -> {
                CompletedListsTab(
                    completedLists = completedLists,
                    colors = colors,
                    onRestore = viewModel::restoreList,
                    onDelete = viewModel::deleteList,
                    onClearHistory = viewModel::clearCompletedLists
                )
            }

            else -> {
                if (showSettings) {
                    SettingsTab(
                        darkTheme = darkTheme,
                        colors = colors,
                        onDarkThemeChange = onDarkThemeChange,
                        onBack = { showSettings = false }
                    )
                } else {
                    ProfileTab(
                        profileName = profileName,
                        listCount = lists.size,
                        totalItems = productCounts.values.sumOf { it.first },
                        boughtItems = productCounts.values.sumOf { it.second },
                        colors = colors,
                        onEditProfile = {
                            editedProfileName = profileName
                            showEditProfileDialog = true
                        },
                        onOpenSettings = { showSettings = true }
                    )
                }
            }
        }

        NavigationBar(
            containerColor = colors.card
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Text("▤", fontSize = 20.sp) },
                label = { Text("Списки") }
            )

            NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Text("◷", fontSize = 20.sp) },
                label = { Text("История") }
            )

            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Text("○", fontSize = 20.sp) },
                label = { Text("Профиль") }
            )
        }
    }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = colors.card,
            title = {
                Text(
                    text = "Редактировать профиль",
                    color = colors.text,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = editedProfileName,
                    onValueChange = { editedProfileName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Твоё имя") },
                    shape = RoundedCornerShape(14.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = editedProfileName.trim()
                        if (name.isNotEmpty()) {
                            profileName = name
                            onProfileNameChange(name)
                            showEditProfileDialog = false
                        }
                    }
                ) {
                    Text("Сохранить", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Отмена", color = colors.muted)
                }
            }
        )
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = colors.card,
            title = {
                Text(
                    text = "Новый список",
                    color = colors.text,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Например, Покупки на выходные") },
                    shape = RoundedCornerShape(14.dp)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val title = newListName.trim()
                        if (title.isNotEmpty()) {
                            viewModel.addList(title)
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Создать", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Отмена", color = colors.muted)
                }
            }
        )
    }

    if (editingList != null) {
        AlertDialog(
            onDismissRequest = { editingList = null },
            containerColor = colors.card,
            title = {
                Text(
                    text = "Редактировать список",
                    color = colors.text,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editListName,
                        onValueChange = { editListName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Название") },
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editListSubtitle,
                        onValueChange = { editListSubtitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Описание") },
                        placeholder = { Text("Например, на неделю") },
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val list = editingList
                        val title = editListName.trim()

                        if (list != null && title.isNotEmpty()) {
                            viewModel.updateList(
                                list = list,
                                title = title,
                                subtitle = editListSubtitle.trim()
                            )
                            editingList = null
                        }
                    }
                ) {
                    Text("Сохранить", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingList = null }) {
                    Text("Отмена", color = colors.muted)
                }
            }
        )
    }

    if (deletingList != null) {
        AlertDialog(
            onDismissRequest = { deletingList = null },
            containerColor = colors.card,
            title = {
                Text(
                    text = "Удалить список?",
                    color = colors.text,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Список «${deletingList?.title}» и все товары в нём будут удалены. Это действие нельзя отменить.",
                    color = colors.muted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingList?.let(viewModel::deleteList)
                        deletingList = null
                    }
                ) {
                    Text("Удалить", color = Color(0xFFB74343))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingList = null }) {
                    Text("Отмена", color = colors.muted)
                }
            }
        )
    }
}

@Composable
private fun ShoppingListRoute(
    list: ShoppingListEntity,
    viewModel: ShoppingViewModel,
    onBack: () -> Unit
) {
    val productEntities by viewModel
        .getProductsForList(list.id)
        .collectAsState(initial = emptyList())

    val products = productEntities.map { entity ->
        ShoppingProduct(
            id = entity.id,
            name = entity.name,
            category = entity.category,
            bought = entity.bought
        )
    }

    ShoppingListScreen(
        listName = list.title,
        products = products,
        onBack = onBack,
        onAddProduct = { name, category ->
            viewModel.addProduct(list.id, name, category)
        },
        onProductBoughtChanged = { productId, bought ->
            productEntities.firstOrNull { it.id == productId }?.let {
                viewModel.setProductBought(it, bought)
            }
        },
        onDeleteBoughtProducts = {
            viewModel.deleteBoughtProducts(list.id)
        },
        onEditProduct = { productId, name, category ->
            productEntities.firstOrNull { it.id == productId }?.let {
                viewModel.updateProductDetails(
                    product = it,
                    name = name,
                    category = category
                )
            }
        },
        onCompleteList = { viewModel.completeList(list) },
        onDeleteProduct = { productId ->
            productEntities.firstOrNull { it.id == productId }?.let {
                viewModel.deleteProduct(it)
            }
        }
    )
}

@Composable
private fun SummaryCard(
    listCount: Int,
    totalItems: Int,
    boughtItems: Int,
    colors: AppPalette
) {
    val progress = if (totalItems == 0) 0f
    else boughtItems.toFloat() / totalItems.toFloat()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.softCard,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Покупки под контролем",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when {
                    totalItems == 0 -> "Добавь товары в свои списки"
                    boughtItems == totalItems -> "Отлично! Всё куплено"
                    else -> "Уже куплено $boughtItems из $totalItems товаров"
                },
                fontSize = 14.sp,
                color = colors.muted
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        colors.progressBackground,
                        RoundedCornerShape(50)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .background(colors.accent, RoundedCornerShape(50))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetric("$listCount", "списков", colors)
                SummaryMetric("$totalItems", "товаров", colors)
                SummaryMetric("${(progress * 100).toInt()}%", "выполнено", colors)
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    value: String,
    label: String,
    colors: AppPalette
) {
    Column {
        Text(
            text = value,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.muted
        )
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingListEntity,
    totalItems: Int,
    boughtItems: Int,
    colors: AppPalette,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val progress = if (totalItems == 0) 0f
    else boughtItems.toFloat() / totalItems.toFloat()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = colors.card,
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        Color(list.color),
                        RoundedCornerShape(17.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = list.icon,
                    fontSize = 24.sp,
                    color = colors.accent
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 8.dp)
            ) {
                Text(
                    text = list.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )
                Text(
                    text = list.subtitle.ifBlank { "Мой список покупок" },
                    fontSize = 13.sp,
                    color = colors.muted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            colors.progressBackground,
                            RoundedCornerShape(50)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .background(colors.accent, RoundedCornerShape(50))
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "$boughtItems из $totalItems куплено",
                    fontSize = 12.sp,
                    color = colors.muted
                )
            }

            Box {
                TextButton(onClick = { menuExpanded = true }) {
                    Text("⋮", fontSize = 25.sp, color = colors.muted)
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Редактировать") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Завершить покупки") },
                        onClick = {
                            menuExpanded = false
                            onComplete()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Удалить", color = Color(0xFFB74343)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyListsCard(colors: AppPalette) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.card,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🛍️", fontSize = 34.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "У тебя пока нет списков",
                color = colors.text,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Создай первый список покупок",
                color = colors.muted,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun CompletedListsTab(
    completedLists: List<ShoppingListEntity>,
    colors: AppPalette,
    onRestore: (ShoppingListEntity) -> Unit,
    onDelete: (ShoppingListEntity) -> Unit,
    onClearHistory: () -> Unit
) {
    var deletingList by remember { mutableStateOf<ShoppingListEntity?>(null) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "История покупок",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

        if (completedLists.isNotEmpty()) {
            TextButton(onClick = { showClearHistoryDialog = true }) {
                Text("Очистить историю", color = Color(0xFFB74343))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Завершённые списки всегда под рукой",
            fontSize = 14.sp,
            color = colors.muted
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (completedLists.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.card,
                shape = RoundedCornerShape(22.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🛍️", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "История пока пуста",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Завершённые покупки появятся здесь",
                        fontSize = 13.sp,
                        color = colors.muted
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = completedLists, key = { it.id }) { list ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.card,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = list.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.text
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = list.subtitle.ifBlank {
                                    "Завершённый список покупок"
                                },
                                fontSize = 13.sp,
                                color = colors.muted
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(onClick = { onRestore(list) }) {
                                    Text("Восстановить", color = colors.accent)
                                }
                                TextButton(onClick = { deletingList = list }) {
                                    Text("Удалить", color = Color(0xFFB74343))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deletingList?.let { list ->
        AlertDialog(
            onDismissRequest = { deletingList = null },
            containerColor = colors.card,
            title = {
                Text("Удалить из истории?", color = colors.text)
            },
            text = {
                Text(
                    "Список «${list.title}» и все товары в нём будут удалены.",
                    color = colors.muted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(list)
                        deletingList = null
                    }
                ) {
                    Text("Удалить", color = Color(0xFFB74343))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingList = null }) {
                    Text("Отмена", color = colors.muted)
                }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = colors.card,
            title = {
                Text(
                    "Очистить историю?",
                    color = colors.text,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Все завершённые списки и товары в них будут удалены. Восстановить их будет нельзя.",
                    color = colors.muted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text("Удалить всё", color = Color(0xFFB74343))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Отмена", color = colors.muted)
                }
            }
        )
    }
}

@Composable
private fun ProfileTab(
    profileName: String,
    listCount: Int,
    totalItems: Int,
    boughtItems: Int,
    colors: AppPalette,
    onEditProfile: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Мой профиль",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.card,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .background(colors.softCard, RoundedCornerShape(42.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profileName.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = profileName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Организую покупки с умом",
                    fontSize = 14.sp,
                    color = colors.muted
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent
                    )
                ) {
                    Text("Редактировать профиль")
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onOpenSettings) {
                    Text(
                        text = "⚙ Настройки",
                        color = colors.accent,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Моя статистика",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.card,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ProfileStatistic("Активные списки", listCount.toString(), colors)
                ProfileStatistic("Всего товаров", totalItems.toString(), colors)
                ProfileStatistic("Куплено товаров", boughtItems.toString(), colors)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Smart Shopping List",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 13.sp,
            color = colors.muted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ProfileStatistic(
    label: String,
    value: String,
    colors: AppPalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp,
            color = colors.muted
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accent
        )
    }
}

@Composable
private fun SettingsTab(
    darkTheme: Boolean,
    colors: AppPalette,
    onDarkThemeChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onBack) {
            Text("← Назад в профиль", color = colors.accent)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Настройки",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.card,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Тёмная тема",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (darkTheme) {
                            "Тёмное оформление включено"
                        } else {
                            "Светлое оформление включено"
                        },
                        fontSize = 14.sp,
                        color = colors.muted
                    )
                }

                Switch(
                    checked = darkTheme,
                    onCheckedChange = onDarkThemeChange
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.card,
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "О приложении",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Smart Shopping List",
                    fontSize = 14.sp,
                    color = colors.muted
                )

                Text(
                    text = "Удобное планирование покупок",
                    fontSize = 14.sp,
                    color = colors.muted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Версия 1.0",
                    fontSize = 13.sp,
                    color = colors.muted
                )
            }
        }
    }
}
