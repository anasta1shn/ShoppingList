
package com.example.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = ShoppingDatabase.getDatabase(applicationContext)
        val repository = ShoppingRepository(
            database.shoppingListDao(),
            database.shoppingProductDao()
        )

        setContent {
            ShoppingListTheme {
                val shoppingViewModel: ShoppingViewModel = viewModel(
                    factory = ShoppingViewModelFactory(repository)
                )

                ShoppingHomeScreen(viewModel = shoppingViewModel)
            }
        }
    }
}

@Composable
private fun ShoppingHomeScreen(viewModel: ShoppingViewModel) {
    val lists by viewModel.shoppingLists.collectAsState()

    var openedListId by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

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
            .background(Cream)
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
                        color = TextDark
                    )

                    Text(
                        text = "Всё необходимое — в одном месте",
                        fontSize = 14.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    val totalItems = productCounts.values.sumOf { it.first }
                    val boughtItems = productCounts.values.sumOf { it.second }

                    SummaryCard(
                        listCount = lists.size,
                        totalItems = totalItems,
                        boughtItems = boughtItems
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
                            color = TextDark
                        )

                        Text(
                            text = "${lists.size} списка",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (lists.isEmpty()) {
                        EmptyListsCard()
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = lists,
                                key = { it.id }
                            ) { list ->
                                val counts = productCounts[list.id] ?: (0 to 0)

                                ShoppingListCard(
                                    list = list,
                                    totalItems = counts.first,
                                    boughtItems = counts.second,
                                    onClick = {
                                        openedListId = list.id
                                    },
                                    onEdit = {
                                        editListName = list.title
                                        editListSubtitle = list.subtitle
                                        editingList = list
                                    },
                                    onDelete = {
                                        deletingList = list
                                    }
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
                            containerColor = DarkGreen
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

            1 -> PlaceholderTab(
                title = "История",
                description = "Здесь появится история покупок"
            )

            else -> PlaceholderTab(
                title = "Профиль",
                description = "Настройки приложения появятся здесь"
            )
        }

        NavigationBar(
            containerColor = CardWhite
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

    // Создание списка
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Новый список",
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text("Например, Покупки на выходные")
                    },
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
                    Text("Создать", color = DarkGreen)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateDialog = false }
                ) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }

    // Редактирование списка
    if (editingList != null) {
        AlertDialog(
            onDismissRequest = { editingList = null },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Редактировать список",
                    color = TextDark,
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
                    Text("Сохранить", color = DarkGreen)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingList = null }
                ) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }

    // Подтверждение удаления списка
    if (deletingList != null) {
        AlertDialog(
            onDismissRequest = { deletingList = null },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Удалить список?",
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Список «${deletingList?.title}» и все товары в нём будут удалены. Это действие нельзя отменить.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingList?.let { list ->
                            viewModel.deleteList(list)

                            if (openedListId == list.id) {
                                openedListId = null
                            }
                        }

                        deletingList = null
                    }
                ) {
                    Text(
                        "Удалить",
                        color = Color(0xFFB74343)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deletingList = null }
                ) {
                    Text("Отмена", color = TextMuted)
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
            val entity = productEntities.firstOrNull {
                it.id == productId
            }

            if (entity != null) {
                viewModel.setProductBought(entity, bought)
            }
        },
        onDeleteBoughtProducts = {
            viewModel.deleteBoughtProducts(list.id)
        }
    )
}

@Composable
private fun SummaryCard(
    listCount: Int,
    totalItems: Int,
    boughtItems: Int
) {
    val progress = if (totalItems == 0) 0f
    else boughtItems.toFloat() / totalItems.toFloat()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SoftGreen,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Покупки под контролем",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when {
                    totalItems == 0 ->
                        "Добавь товары в свои списки"

                    boughtItems == totalItems ->
                        "Отлично! Всё куплено"

                    else ->
                        "Уже куплено $boughtItems из $totalItems товаров"
                },
                fontSize = 14.sp,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        Color(0xFFD2DCCB),
                        RoundedCornerShape(50)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .background(
                            DarkGreen,
                            RoundedCornerShape(50)
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetric(
                    value = "$listCount",
                    label = "списков"
                )
                SummaryMetric(
                    value = "$totalItems",
                    label = "товаров"
                )
                SummaryMetric(
                    value = "${(progress * 100).toInt()}%",
                    label = "выполнено"
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    value: String,
    label: String
) {
    Column {
        Text(
            text = value,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen
        )

        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingListEntity,
    totalItems: Int,
    boughtItems: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val progress = if (totalItems == 0) 0f
    else boughtItems.toFloat() / totalItems.toFloat()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = CardWhite,
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
                    color = DarkGreen
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
                    color = TextDark
                )

                Text(
                    text = list.subtitle.ifBlank {
                        "Мой список покупок"
                    },
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            Color(0xFFE9EAE3),
                            RoundedCornerShape(50)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .background(
                                DarkGreen,
                                RoundedCornerShape(50)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "$boughtItems из $totalItems куплено",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Box {
                TextButton(
                    onClick = { menuExpanded = true }
                ) {
                    Text(
                        text = "⋮",
                        fontSize = 25.sp,
                        color = TextMuted
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = {
                        menuExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text("Редактировать") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                "Удалить",
                                color = Color(0xFFB74343)
                            )
                        },
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
private fun EmptyListsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardWhite,
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
                color = TextDark,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Создай первый список покупок",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PlaceholderTab(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = description,
            fontSize = 14.sp,
            color = TextMuted
        )
    }
}
