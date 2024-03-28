package com.intro.restaurant.data.model;

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.intro.restaurant.data.db.RestaurantDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestaurantViewModel(private val restaurantDao: RestaurantDao) : ViewModel() {
    val allRestaurants: LiveData<List<RestaurantModel>> = restaurantDao.getAll()

    fun fetchRestaurantsFromFirestore(userEmail: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            db.collection("Restaurants").get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val restaurants = mutableListOf<RestaurantModel>() // List of restaurants to be fetched

                    for (document in task.result!!) {
                        val imageUrl = document.getString("imageUrl")

                        val restaurant = RestaurantModel(
                            key = document.id,
                            userEmail = document.getString("userEmail") ?: "",
                            imageUrl = imageUrl ?: "",
                            menu = document.getString("menu") ?: "",
                            latitude = document.getDouble("latitude") ?: 0.0,
                            longitude = document.getDouble("longitude") ?: 0.0,
                            name = document.getString("name") ?: "",
                            address = document.getString("address") ?: "",
                            foodtype = document.getString("foodtype") ?: "",
                            review = document.getString("review") ?: "",
                            favourite = document.getBoolean("favourite") ?: false
                        )
                        restaurants.add(restaurant)
                    }

                    // Insert restaurants into database within IO scope
                    CoroutineScope(Dispatchers.IO).launch {
                        restaurantDao.deleteAll()
                        restaurantDao.insertAll(restaurants)
                    }.invokeOnCompletion {
                        // Do something after insertion completes if needed
                    }
                } else {
                    Log.e("RestaurantViewModel", "Error fetching restaurants")
                }
            }
        }
    }

    fun fetchMyRestaurantsFromFirestore(userEmail: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            db.collection("Restaurants").whereEqualTo("userEmail", userEmail).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val restaurants = mutableListOf<RestaurantModel>() // List of restaurants to be fetched

                    for (document in task.result!!) {
                        val imageUrl = document.getString("imageUrl")

                        val restaurant = RestaurantModel(
                            key = document.id,
                            userEmail = document.getString("userEmail") ?: "",
                            imageUrl = imageUrl ?: "",
                            menu = document.getString("menu") ?: "",
                            latitude = document.getDouble("latitude") ?: 0.0,
                            longitude = document.getDouble("longitude") ?: 0.0,
                            name = document.getString("name") ?: "",
                            address = document.getString("address") ?: "",
                            foodtype = document.getString("foodtype") ?: "",
                            review = document.getString("review") ?: "",
                            favourite = document.getBoolean("favourite") ?: false
                        )
                        restaurants.add(restaurant)
                    }

                    // Insert restaurants into database within IO scope
                    CoroutineScope(Dispatchers.IO).launch {
                        restaurantDao.deleteAll()
                        restaurantDao.insertAll(restaurants)
                    }.invokeOnCompletion {
                        // Do something after insertion completes if needed
                    }
                } else {
                    Log.e("RestaurantViewModel", "Error fetching restaurants")
                }
            }
        }
    }

    fun fetchFavouriteRestaurantsForUserEmail(userEmail: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            db.collection("Restaurants").whereEqualTo("favourite", true) .get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val restaurants = mutableListOf<RestaurantModel>() // List of restaurants to be fetched

                    for (document in task.result!!) {
                        val imageName = document.getString("imageUrl")

                        val restaurant = RestaurantModel(
                            key = document.id,
                            userEmail = document.getString("userEmail") ?: "",
                            imageUrl = imageName ?: "",
                            menu = document.getString("menu") ?: "",
                            latitude = document.getDouble("latitude") ?: 0.0,
                            longitude = document.getDouble("longitude") ?: 0.0,
                            name = document.getString("name") ?: "",
                            address = document.getString("address") ?: "",
                            foodtype = document.getString("foodtype") ?: "",
                            review = document.getString("review") ?: "",
                            favourite = document.getBoolean("favourite") ?: true
                        )
                        restaurants.add(restaurant)
                    }

                    // Insert restaurants into database within IO scope
                    CoroutineScope(Dispatchers.IO).launch {
                        restaurantDao.deleteAll()
                        restaurantDao.insertAll(restaurants)
                    }.invokeOnCompletion {
                        // Do something after insertion completes if needed
                    }
                } else {
                    Log.e("RestaurantViewModel", "Error fetching restaurants for user email: $userEmail")
                }
            }
        }
    }
}
