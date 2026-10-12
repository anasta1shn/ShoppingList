
package com.example.shoppinglist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoppinglist.ui.theme.ShoppingListTheme

private val Cream = Color(0xFFF7F6F0)
private val DarkGreen = Color(0xFF42664D)
private val SoftGreen = Color(0xFFE6EBDD)
private val TextDark = Color(0xFF28352B)
private val TextMuted = Color(0xFF899087)
private val CardWhite = Color(0xFFFFFEFA)
private val SoftPeach = Color(0xFFF5E4D9)

private data class ShoppingListItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val icon: String,
    val color: Color,
    val totalItems: Int,
    val boughtItems: Int
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ShoppingListTheme {
                ShoppingHomeScreen()
            }
        }
    }
}

@Composable
fun ShoppingHomeScreen() {
    val shoppingLists = remember {
        mutableStateListOf(
            ShoppingListItem(
                1, "На неделю", "Основные продукты",
                "▤", SoftGreen, 12, 5
            ),
            ShoppingListItem(
                2, "Для дома", "Всё для уюта",
                "⌂", SoftPeach, 6, 2
            ),
            ShoppingListItem(
                3, "Для праздника", "Что-то особенное",
                "✳", Color(0xFFEAE6F2), 8, 0
            )
        )
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var nextId by remember { mutableIntStateOf(4) }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            NavigationBarSimple(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(innerPadding)
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Shopping",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Покупки без лишних забот",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SoftGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♡",
                        color = DarkGreen,
                        fontSize = 26.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            SummaryCard(shoppingLists)

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> "Мои списки"
                        1 -> "История"
                        else -> "Профиль"
                    },
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = if (selectedTab == 0) {
                        "${shoppingLists.size} списка"
                    } else {
                        "Smart Shopping"
                    },
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = shoppingLists,
                        key = { it.id }
                    ) { list ->
                        ShoppingListCard(list = list)
                    }

                    item {
                        Button(
                            onClick = {
                                shoppingLists.add(
                                    ShoppingListItem(
                                        id = nextId++,
                                        title = "Новый список",
                                        subtitle = "Мой список покупок",
                                        icon = "+",
                                        color = SoftGreen,
                                        totalItems = 0,
                                        boughtItems = 0
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkGreen
                            )
                        ) {
                            Text(
                                text = "+   Создать список",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }
            } else {
                PlaceholderTab(
                    tab = selectedTab,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(lists: List<ShoppingListItem>) {
    val total = lists.sumOf { it.totalItems }
    val bought = lists.sumOf { it.boughtItems }
    val progress = if (total == 0) 0f else bought.toFloat() / total

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SoftGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 25.sp,
                        color = DarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Всё под контролем",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Твои покупки в одном месте",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Text(
                    text = "$bought/$total",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .background(SoftGreen, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(7.dp)
                        .background(DarkGreen, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = if (total == 0) {
                    "Пока нет товаров"
                } else {
                    "Куплено ${"%.0f".format(progress * 100)}% товаров"
                },
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun ShoppingListCard(list: ShoppingListItem) {
    val progress = if (list.totalItems == 0) {
        0f
    } else {
        list.boughtItems.toFloat() / list.totalItems
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Переход к содержимому списка добавим позже.
            },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .background(
                        list.color,
                        RoundedCornerShape(17.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = list.icon,
                    fontSize = 27.sp,
                    color = DarkGreen
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = list.subtitle,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(SoftGreen, CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(5.dp)
                            .background(DarkGreen, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "${list.boughtItems} из ${list.totalItems} куплено",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "›",
                fontSize = 27.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun NavigationBarSimple(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        color = CardWhite,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem(
                icon = "▤",
                label = "Списки",
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )

            NavigationItem(
                icon = "◷",
                label = "История",
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )

            NavigationItem(
                icon = "♙",
                label = "Профиль",
                selected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
private fun NavigationItem(
    icon: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) DarkGreen else TextMuted

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 22.sp,
            color = contentColor
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = contentColor,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            }
        )
    }
}

@Composable
private fun PlaceholderTab(
    tab: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (tab == 1) "◷" else "♡",
            fontSize = 42.sp,
            color = DarkGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (tab == 1) {
                "История покупок"
            } else {
                "Твой профиль"
            },
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Добавим этот раздел чуть позже",
            fontSize = 13.sp,
            color = TextMuted
        )
    }
}