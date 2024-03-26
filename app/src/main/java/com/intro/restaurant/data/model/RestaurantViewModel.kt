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
    val allProblems: LiveData<List<RestaurantModel>> = restaurantDao.getAll()

    fun fetchProblemsFromFirestore(userEmail: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            db.collection("Problems").whereNotEqualTo("userEmail", userEmail).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val problems = mutableListOf<RestaurantModel>() // List of problems to be fetched

                    for (document in task.result!!) {
                        val imageUrl = document.getString("imageUrl")

                        val problem = RestaurantModel(
                            key = document.id,
                            userEmail = document.getString("userEmail") ?: "",
                            imageUrl = imageUrl ?: "",
                            description = document.getString("description") ?: "",
                            latitude = document.getDouble("latitude") ?: 0.0,
                            longitude = document.getDouble("longitude") ?: 0.0,
                            title = document.getString("title") ?: "",
                            dateStarted = document.getString("dateStarted") ?: "",
                            ageOfRestaurant = document.getString("ageOfRestaurant") ?: "",
                            suggestion = document.getString("suggestion") ?: "",
                            expertName = document.getString("expertName") ?: ""
                        )
                        problems.add(problem)
                    }

                    // Insert problems into database within IO scope
                    CoroutineScope(Dispatchers.IO).launch {
                        restaurantDao.deleteAll()
                        restaurantDao.insertAll(problems)
                    }.invokeOnCompletion {
                        // Do something after insertion completes if needed
                    }
                } else {
                    Log.e("RestaurantViewModel", "Error fetching problems")
                }
            }
        }
    }

    fun fetchMyProblemsForUserEmail(userEmail: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            db.collection("Problems").whereEqualTo("userEmail", userEmail).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val problems = mutableListOf<RestaurantModel>() // List of problems to be fetched

                    for (document in task.result!!) {
                        val imageName = document.getString("imageUrl")

                        val problem = RestaurantModel(
                            key = document.id,
                            userEmail = document.getString("userEmail") ?: "",
                            imageUrl = imageName ?: "",
                            description = document.getString("description") ?: "",
                            latitude = document.getDouble("latitude") ?: 0.0,
                            longitude = document.getDouble("longitude") ?: 0.0,
                            title = document.getString("title") ?: "",
                            dateStarted = document.getString("dateStarted") ?: "",
                            ageOfRestaurant = document.getString("ageOfRestaurant") ?: "",
                            suggestion = document.getString("suggestion") ?: "",
                            expertName = document.getString("expertName") ?: ""
                        )
                        problems.add(problem)
                    }

                    // Insert problems into database within IO scope
                    CoroutineScope(Dispatchers.IO).launch {
                        restaurantDao.deleteAll()
                        restaurantDao.insertAll(problems)
                    }.invokeOnCompletion {
                        // Do something after insertion completes if needed
                    }
                } else {
                    Log.e("RestaurantViewModel", "Error fetching problems for user email: $userEmail")
                }
            }
        }
    }
}
