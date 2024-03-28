package com.intro.restaurant.presentation.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.intro.restaurant.R

class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val restaurantImage: ImageView = itemView.findViewById(R.id.restaurantImage)
    val restaurantName: TextView = itemView.findViewById(R.id.name)
}
