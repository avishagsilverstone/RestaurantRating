package com.intro.restaurant.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.intro.restaurant.data.model.RestaurantModel

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM Restaurant")
    fun getAll(): LiveData<List<RestaurantModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(restaurants: List<RestaurantModel>)



    @Query("DELETE FROM Restaurant")
    fun deleteAll()

    @Delete
    fun delete(restaurant: RestaurantModel)

    @Update
    fun update(restaurant: RestaurantModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(restaurant: RestaurantModel)
}
