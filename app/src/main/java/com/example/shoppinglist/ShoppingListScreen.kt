
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
private val SoftPeach = Color(0xFFF5E4D9)

@Composable
fun ShoppingListScreen(
    listName: String,
    products: List<ShoppingProduct>,
    onBack: () -> Unit,
    onAddProduct: (String, String) -> Unit,
    onProductBoughtChanged: (Int, Boolean) -> Unit,
    onDeleteBoughtProducts: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Другое") }

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

        Spacer(modifier = Modifier.height(20.dp))

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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
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
                                fontWeight = if (selected) FontWeight.SemiBold
                                else FontWeight.Normal
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val groupedProducts = products.groupBy { it.category }

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
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CardWhite,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onProductBoughtChanged(
                                            product.id,
                                            !product.bought
                                        )
                                    }
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
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

                                Text(
                                    text = product.name,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 6.dp),
                                    color = if (product.bought) TextMuted
                                    else TextDark,
                                    fontSize = 15.sp,
                                    textDecoration = if (product.bought) {
                                        TextDecoration.LineThrough
                                    } else {
                                        TextDecoration.None
                                    }
                                )

                                if (product.bought) {
                                    Text("✓", color = DarkGreen, fontSize = 18.sp)
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
}
