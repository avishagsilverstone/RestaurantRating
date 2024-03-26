package com.intro.restaurant.data.model
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.intro.restaurant.data.db.RestaurantDao

class RestaurantViewModelFactory(private val restaurantDao: RestaurantDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RestaurantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RestaurantViewModel(restaurantDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
