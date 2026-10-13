
package com.example.shoppinglist.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingProductDao {

    @Query(
        "SELECT * FROM shopping_products " +
                "WHERE listId = :listId ORDER BY id DESC"
    )
    fun getProductsForList(listId: Int): Flow<List<ShoppingProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ShoppingProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ShoppingProductEntity>)

    @Update
    suspend fun updateProduct(product: ShoppingProductEntity)

    @Delete
    suspend fun deleteProduct(product: ShoppingProductEntity)

    @Query("DELETE FROM shopping_products WHERE listId = :listId AND bought = 1")
    suspend fun deleteBoughtProducts(listId: Int)

    @Query("DELETE FROM shopping_products WHERE listId = :listId")
    suspend fun deleteAllProductsForList(listId: Int)
}
