package com.intro.restaurant.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Restaurant")
data class RestaurantModel(
        @PrimaryKey(autoGenerate = true) var id: Int = 0,
        var key: String? = null,
        var userEmail: String,
        var imageUrl: String,
        var name: String,
        var menu: String,
        var latitude: Double,
        var longitude: Double,
        var address: String,
        var foodtype: String,
        var review: String? = null,
        var favourite: Boolean = false
) {

        constructor(
                key: String,
                userEmail: String,
                imageUrl: String,
                name: String,
                menu: String,
                latitude: Double,
                longitude: Double,
                address: String,
                foodtype: String,
                review: String? = null,
                favourite: Boolean = false
        ) : this(0, key, userEmail, imageUrl,name,  menu, latitude, longitude, address, foodtype, review, favourite)
}

