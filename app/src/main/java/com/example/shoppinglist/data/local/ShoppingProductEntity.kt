
package com.example.shoppinglist.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_products",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["listId"])]
)
data class ShoppingProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val listId: Int,

    val name: String,

    val category: String,

    val bought: Boolean = false
)
