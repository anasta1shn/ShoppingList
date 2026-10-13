
package com.example.shoppinglist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val subtitle: String = "",

    val icon: String = "shopping_cart",

    val color: Long = 0xFFE6EBDD,

    val createdAt: Long = System.currentTimeMillis()
)
