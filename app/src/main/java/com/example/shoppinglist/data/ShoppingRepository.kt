
package com.example.shoppinglist.data

import com.example.shoppinglist.data.local.ShoppingListDao
import com.example.shoppinglist.data.local.ShoppingListEntity
import com.example.shoppinglist.data.local.ShoppingProductDao
import com.example.shoppinglist.data.local.ShoppingProductEntity
import kotlinx.coroutines.flow.Flow

class ShoppingRepository(
    private val shoppingListDao: ShoppingListDao,
    private val shoppingProductDao: ShoppingProductDao
) {

    // Активные списки
    val allLists: Flow<List<ShoppingListEntity>> =
        shoppingListDao.getAllLists()

    // Завершённые списки для истории
    val completedLists: Flow<List<ShoppingListEntity>> =
        shoppingListDao.getCompletedLists()

    fun getListById(listId: Int): Flow<ShoppingListEntity?> =
        shoppingListDao.getListById(listId)

    suspend fun insertList(list: ShoppingListEntity): Long =
        shoppingListDao.insertList(list)

    suspend fun getListsCount(): Int =
        shoppingListDao.getListsCount()

    suspend fun updateList(list: ShoppingListEntity) {
        shoppingListDao.updateList(list)
    }

    suspend fun deleteList(list: ShoppingListEntity) {
        shoppingListDao.deleteList(list)
    }

    fun getProductsForList(listId: Int): Flow<List<ShoppingProductEntity>> =
        shoppingProductDao.getProductsForList(listId)

    suspend fun insertProduct(product: ShoppingProductEntity): Long =
        shoppingProductDao.insertProduct(product)

    suspend fun insertProducts(products: List<ShoppingProductEntity>) {
        shoppingProductDao.insertProducts(products)
    }

    suspend fun updateProduct(product: ShoppingProductEntity) {
        shoppingProductDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ShoppingProductEntity) {
        shoppingProductDao.deleteProduct(product)
    }

    suspend fun deleteBoughtProducts(listId: Int) {
        shoppingProductDao.deleteBoughtProducts(listId)
    }

    suspend fun deleteAllProductsForList(listId: Int) {
        shoppingProductDao.deleteAllProductsForList(listId)
    }
}
