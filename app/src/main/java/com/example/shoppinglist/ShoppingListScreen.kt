
package com.example.shoppinglist

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ShoppingProduct(
    val id: Int,
    val name: String,
    val category: String,
    val bought: Boolean = false
)

private val Cream = Color(0xFFF7F6F0)
private val DarkGreen = Color(0xFF42664D)
private val SoftGreen = Color(0xFFE6EBDD)
private val TextDark = Color(0xFF28352B)
private val TextMuted = Color(0xFF899087)
private val CardWhite = Color(0xFFFFFEFA)

@Composable
fun ShoppingListScreen(
    listName: String,
    products: List<ShoppingProduct>,
    onBack: () -> Unit,
    onCompleteList: () -> Unit,
    onAddProduct: (String, String) -> Unit,
    onProductBoughtChanged: (Int, Boolean) -> Unit,
    onDeleteBoughtProducts: () -> Unit,
    onEditProduct: (Int, String, String) -> Unit,
    onDeleteProduct: (Int) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Другое") }
    var searchQuery by remember { mutableStateOf("") }

    var editingProduct by remember {
        mutableStateOf<ShoppingProduct?>(null)
    }
    var editProductName by remember { mutableStateOf("") }
    var editProductCategory by remember { mutableStateOf("Другое") }

    var deletingProduct by remember {
        mutableStateOf<ShoppingProduct?>(null)
    }
    var showCompleteDialog by remember { mutableStateOf(false) }

    val boughtCount = products.count { it.bought }
    val totalCount = products.size

    val categories = listOf(
        "Молочные продукты",
        "Овощи и фрукты",
        "Мясо и рыба",
        "Напитки",
        "Для дома",
        "Другое"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок и кнопка назад
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onBack() },
                color = CardWhite,
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("‹", fontSize = 32.sp, color = DarkGreen)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = listName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Text(
                    text = "$boughtCount из $totalCount куплено",
                    fontSize = 14.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Завершение покупок
        if (totalCount > 0) {
            Button(
                onClick = { showCompleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen
                )
            ) {
                Text(
                    text = "✓  Завершить покупки",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Добавление товара
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardWhite,
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Добавить товар",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Например, хлеб") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val name = productName.trim()
                            if (name.isNotEmpty()) {
                                onAddProduct(name, selectedCategory)
                                productName = ""
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Категория",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val selected = selectedCategory == category

                        Surface(
                            modifier = Modifier.clickable {
                                selectedCategory = category
                            },
                            color = if (selected) SoftGreen else Cream,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                ),
                                color = if (selected) DarkGreen else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (selected) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val name = productName.trim()
                        if (name.isNotEmpty()) {
                            onAddProduct(name, selectedCategory)
                            productName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen
                    )
                ) {
                    Text("Добавить товар")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Поиск товаров
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Найти товар...")
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))


        // Заголовок товаров
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Товары",
                modifier = Modifier.weight(1f),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            if (boughtCount > 0) {
                TextButton(onClick = onDeleteBoughtProducts) {
                    Text("Удалить купленные", color = DarkGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (products.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardWhite,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🛒", fontSize = 34.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Список пока пуст",
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )

                    Text(
                        text = "Добавь товары, чтобы ничего не забыть",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val filteredProducts = products.filter { product ->
                    product.name.contains(searchQuery.trim(), ignoreCase = true) ||
                            product.category.contains(searchQuery.trim(), ignoreCase = true)
                }

                val groupedProducts = filteredProducts.groupBy { it.category }

                groupedProducts.forEach { (category, categoryProducts) ->
                    item(key = "category_$category") {
                        Text(
                            text = category,
                            modifier = Modifier.padding(
                                top = 8.dp,
                                bottom = 2.dp,
                                start = 4.dp
                            ),
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    items(
                        items = categoryProducts,
                        key = { it.id }
                    ) { product ->
                        var menuExpanded by remember {
                            mutableStateOf(false)
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CardWhite,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 6.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = product.bought,
                                    onCheckedChange = { checked ->
                                        onProductBoughtChanged(
                                            product.id,
                                            checked
                                        )
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = DarkGreen
                                    )
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            onProductBoughtChanged(
                                                product.id,
                                                !product.bought
                                            )
                                        }
                                ) {
                                    Text(
                                        text = product.name,
                                        color = if (product.bought) {
                                            TextMuted
                                        } else {
                                            TextDark
                                        },
                                        fontSize = 15.sp,
                                        textDecoration = if (product.bought) {
                                            TextDecoration.LineThrough
                                        } else {
                                            TextDecoration.None
                                        }
                                    )

                                    Text(
                                        text = product.category,
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }

                                if (product.bought) {
                                    Text(
                                        text = "✓",
                                        color = DarkGreen,
                                        fontSize = 18.sp
                                    )
                                }

                                Box {
                                    TextButton(
                                        onClick = {
                                            menuExpanded = true
                                        }
                                    ) {
                                        Text(
                                            text = "⋮",
                                            fontSize = 24.sp,
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
                                            text = {
                                                Text("Редактировать")
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                editProductName = product.name
                                                editProductCategory =
                                                    product.category
                                                editingProduct = product
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
                                                deletingProduct = product
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    // Подтверждение завершения покупок
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showCompleteDialog = false
            },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Завершить покупки?",
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Список «$listName» переместится в историю. Его можно будет восстановить позже.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCompleteDialog = false
                        onCompleteList()
                    }
                ) {
                    Text("Завершить", color = DarkGreen)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCompleteDialog = false
                    }
                ) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }

    // Диалог редактирования товара
    if (editingProduct != null) {
        AlertDialog(
            onDismissRequest = {
                editingProduct = null
            },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Редактировать товар",
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editProductName,
                        onValueChange = { editProductName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Название товара") },
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Категория",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            val selected = editProductCategory == category

                            Surface(
                                modifier = Modifier.clickable {
                                    editProductCategory = category
                                },
                                color = if (selected) SoftGreen else Cream,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    ),
                                    color = if (selected) {
                                        DarkGreen
                                    } else {
                                        TextMuted
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val product = editingProduct
                        val name = editProductName.trim()

                        if (product != null && name.isNotEmpty()) {
                            onEditProduct(
                                product.id,
                                name,
                                editProductCategory
                            )
                            editingProduct = null
                        }
                    }
                ) {
                    Text("Сохранить", color = DarkGreen)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingProduct = null }
                ) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }

    // Подтверждение удаления товара
    if (deletingProduct != null) {
        AlertDialog(
            onDismissRequest = {
                deletingProduct = null
            },
            containerColor = CardWhite,
            title = {
                Text(
                    text = "Удалить товар?",
                    color = TextDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Удалить «${deletingProduct?.name}» из списка?",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletingProduct?.let { product ->
                            onDeleteProduct(product.id)
                        }
                        deletingProduct = null
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
                    onClick = { deletingProduct = null }
                ) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }
}
