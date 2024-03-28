package com.intro.restaurant.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.intro.restaurant.data.model.RestaurantModel

@Database(entities = [RestaurantModel::class], version = 4)
abstract class MyDatabse : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabse? = null

        // Migration from version 3 to 4
        val migration3to4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Restaurant ADD COLUMN new_column_name TEXT DEFAULT ''")
            }
        }

        fun getInstance(context: Context): MyDatabse {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabse::class.java,
                    "restaurant_database" // Change database name to "restaurant_database"
                ).addMigrations(migration3to4) // Add migration from version 3 to 4
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
