
package com.example.shoppinglist.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoppinglist.data.ShoppingRepository
import com.example.shoppinglist.data.local.ShoppingListEntity
import com.example.shoppinglist.data.local.ShoppingProductEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(
    private val repository: ShoppingRepository
) : ViewModel() {

    val shoppingLists = repository.allLists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun getProductsForList(listId: Int) =
        repository.getProductsForList(listId)

    fun addList(title: String) {
        viewModelScope.launch {
            repository.insertList(
                ShoppingListEntity(title = title)
            )
        }
    }

    fun updateList(
        list: ShoppingListEntity,
        title: String,
        subtitle: String
    ) {
        viewModelScope.launch {
            repository.updateList(
                list.copy(
                    title = title.trim(),
                    subtitle = subtitle.trim()
                )
            )
        }
    }

    fun deleteList(list: ShoppingListEntity) {
        viewModelScope.launch {
            repository.deleteList(list)
        }
    }

    fun addProduct(
        listId: Int,
        name: String,
        category: String
    ) {
        viewModelScope.launch {
            repository.insertProduct(
                ShoppingProductEntity(
                    listId = listId,
                    name = name,
                    category = category
                )
            )
        }
    }

    fun toggleProduct(product: ShoppingProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(
                product.copy(bought = !product.bought)
            )
        }
    }

    fun setProductBought(
        product: ShoppingProductEntity,
        bought: Boolean
    ) {
        viewModelScope.launch {
            repository.updateProduct(
                product.copy(bought = bought)
            )
        }
    }

    fun deleteProduct(product: ShoppingProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun deleteBoughtProducts(listId: Int) {
        viewModelScope.launch {
            repository.deleteBoughtProducts(listId)
        }
    }


    fun updateProductDetails(
        product: ShoppingProductEntity,
        name: String,
        category: String
    ) {
        val updatedProduct = product.copy(
            name = name.trim(),
            category = category
        )

        viewModelScope.launch {
            repository.updateProduct(updatedProduct)
        }
    }

    fun createInitialDataIfEmpty() {
        viewModelScope.launch {
            if (repository.allLists.first().isEmpty()) {
                val weeklyId = repository.insertList(
                    ShoppingListEntity(
                        title = "На неделю",
                        subtitle = "Основные продукты",
                        icon = "▤",
                        color = 0xFFE6EBDD
                    )
                ).toInt()

                val homeId = repository.insertList(
                    ShoppingListEntity(
                        title = "Для дома",
                        subtitle = "Всё для уюта",
                        icon = "⌂",
                        color = 0xFFF5E4D9
                    )
                ).toInt()

                val partyId = repository.insertList(
                    ShoppingListEntity(
                        title = "Для праздника",
                        subtitle = "Что-то особенное",
                        icon = "✳",
                        color = 0xFFEAE6F2
                    )
                ).toInt()

                val initialProducts = listOf(
                    ShoppingProductEntity(
                        listId = weeklyId,
                        name = "Молоко",
                        category = "Молочные продукты"
                    ),
                    ShoppingProductEntity(
                        listId = weeklyId,
                        name = "Сыр",
                        category = "Молочные продукты"
                    ),
                    ShoppingProductEntity(
                        listId = weeklyId,
                        name = "Йогурт",
                        category = "Молочные продукты"
                    ),
                    ShoppingProductEntity(
                        listId = weeklyId,
                        name = "Яблоки",
                        category = "Овощи и фрукты"
                    ),
                    ShoppingProductEntity(
                        listId = weeklyId,
                        name = "Бананы",
                        category = "Овощи и фрукты"
                    ),
                    ShoppingProductEntity(
                        listId = weeklyId,
                        name = "Помидоры",
                        category = "Овощи и фрукты"
                    ),
                    ShoppingProductEntity(
                        listId = weeklyId,
                        name = "Куриное филе",
                        category = "Мясо и рыба"
                    ),
                    ShoppingProductEntity(
                        listId = weeklyId,
                        name = "Хлеб",
                        category = "Бакалея"
                    ),
                    ShoppingProductEntity(
                        listId = homeId,
                        name = "Губка для посуды",
                        category = "Уборка"
                    ),
                    ShoppingProductEntity(
                        listId = homeId,
                        name = "Средство для пола",
                        category = "Уборка"
                    ),
                    ShoppingProductEntity(
                        listId = homeId,
                        name = "Мыло",
                        category = "Гигиена"
                    ),
                    ShoppingProductEntity(
                        listId = partyId,
                        name = "Торт",
                        category = "Сладости"
                    ),
                    ShoppingProductEntity(
                        listId = partyId,
                        name = "Сок",
                        category = "Напитки"
                    ),
                    ShoppingProductEntity(
                        listId = partyId,
                        name = "Фрукты",
                        category = "Овощи и фрукты"
                    )
                )

                repository.insertProducts(initialProducts)
            }
        }
    }
}
