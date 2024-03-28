package com.intro.restaurant.presentation.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.intro.restaurant.R
import com.intro.restaurant.data.model.RestaurantModel
import com.squareup.picasso.Picasso

class RestaurantsAdapter(
    private val fragmentActivity: FragmentActivity,
    var restaurants: List<RestaurantModel>,
    private val isEdit: Boolean
) : RecyclerView.Adapter<RestaurantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurants, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        Picasso.get().load(restaurant.imageUrl).into(holder.restaurantImage)
        holder.restaurantName.text = restaurant.name
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putString("restaurantId", restaurant.key)
                putString("name", restaurant.name)
                putString("userEmail", restaurant.userEmail)
                putString("imageUrl", restaurant.imageUrl)
                putString("menu", restaurant.menu)
                putString("address", restaurant.address)
                putString("foodtype", restaurant.foodtype)
                putString("review", restaurant.review)
                putBoolean("favourite", restaurant.favourite)
                putBoolean("isEdit", isEdit)
            }
            val navController = fragmentActivity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController.navController.navigate(R.id.restaurantDetailFragment, bundle)
        }
    }

    override fun getItemCount(): Int {
        return restaurants.size
    }
}
