package com.intro.restaurant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "RestaurantProblem")
data class RestaurantModel(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        var key: String? = null,
        var userEmail: String,
        var imageUrl: String,
        var title: String,
        var description: String,
        var latitude: Double,
        var longitude: Double,
        var dateStarted: String,
        var ageOfRestaurant: String,
        var suggestion: String? = null,
        var expertName: String? = null // ExpertName field 추가
) {

        constructor(
                key: String,
                userEmail: String,
                imageUrl: String,
                title: String,
                description: String,
                latitude: Double,
                longitude: Double,
                dateStarted: String,
                ageOfRestaurant: String,
                suggestion: String? = null,
                expertName: String? = null
        ) : this(0, key, userEmail, imageUrl,title,  description, latitude, longitude, dateStarted, ageOfRestaurant, suggestion, expertName)
}

