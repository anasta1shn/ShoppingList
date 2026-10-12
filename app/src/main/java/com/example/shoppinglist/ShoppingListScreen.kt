
package com.example.shoppinglist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

private val ShoppingGreen = Color(0xFF42664D)
private val ShoppingCream = Color(0xFFF7F6F0)
private val ShoppingText = Color(0xFF28352B)
private val ShoppingMuted = Color(0xFF899087)

data class ShoppingProduct(
    val id: Int,
    val name: String,
    val category: String,
    val bought: Boolean = false
)

@Composable
fun ShoppingListScreen(
    listName: String,
    products: SnapshotStateList<ShoppingProduct>,
    onBack: () -> Unit,
    onItemsChanged: (Int, Int) -> Unit
) {
    var newItemName by remember { mutableStateOf("") }

    val boughtCount = products.count { it.bought }
    val categories = products.map { it.category }.distinct()

    LaunchedEffect(products.size, boughtCount) {
        onItemsChanged(products.size, boughtCount)
    }

    Scaffold(
        containerColor = ShoppingCream,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = ShoppingGreen
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = listName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = ShoppingText
                    )
                    Text(
                        text = "$boughtCount из ${products.size} куплено",
                        color = ShoppingMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Добавить товар",
                        fontWeight = FontWeight.SemiBold,
                        color = ShoppingText
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Например, яйца") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val name = newItemName.trim()

                            if (name.isNotEmpty()) {
                                products.add(
                                    ShoppingProduct(
                                        id = (products.maxOfOrNull { it.id } ?: 0) + 1,
                                        name = name,
                                        category = "Другое"
                                    )
                                )
                                newItemName = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Text(
                            text = "Добавить",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    val categoryItems = products.filter {
                        it.category == category
                    }

                    Column {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ShoppingGreen,
                            modifier = Modifier.padding(
                                start = 4.dp,
                                bottom = 6.dp
                            )
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column {
                                categoryItems.forEachIndexed { index, product ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val productIndex =
                                                    products.indexOfFirst {
                                                        it.id == product.id
                                                    }

                                                if (productIndex >= 0) {
                                                    products[productIndex] =
                                                        product.copy(
                                                            bought = !product.bought
                                                        )
                                                }
                                            }
                                            .padding(
                                                horizontal = 12.dp,
                                                vertical = 4.dp
                                            ),
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = product.bought,
                                            onCheckedChange = { checked ->
                                                val productIndex =
                                                    products.indexOfFirst {
                                                        it.id == product.id
                                                    }

                                                if (productIndex >= 0) {
                                                    products[productIndex] =
                                                        product.copy(
                                                            bought = checked
                                                        )
                                                }
                                            }
                                        )

                                        Text(
                                            text = product.name,
                                            modifier = Modifier.weight(1f),
                                            color = if (product.bought) {
                                                ShoppingMuted
                                            } else {
                                                ShoppingText
                                            },
                                            textDecoration =
                                                if (product.bought) {
                                                    TextDecoration.LineThrough
                                                } else {
                                                    TextDecoration.None
                                                }
                                        )
                                    }

                                    if (index < categoryItems.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp
                                            ),
                                            color = ShoppingCream
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            products.removeAll { it.bought }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Удалить купленные товары",
                            color = ShoppingGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}