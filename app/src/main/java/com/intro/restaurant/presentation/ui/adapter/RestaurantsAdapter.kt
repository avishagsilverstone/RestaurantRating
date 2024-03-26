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
    var problems: List<RestaurantModel>,
    private val isEdit: Boolean
) : RecyclerView.Adapter<RestaurantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurants, parent, false)
        return RestaurantViewHolder(view)
    }

    fun updateProblems(problems: List<RestaurantModel>) {
        this.problems = problems
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val problem = problems[position]

        // Load image using Picasso library
        Picasso.get().load(problem.imageUrl).into(holder.problemImage)

        // Set problem title
        holder.problemName.text = problem.title

        // Set click listener to navigate to problem detail fragment
        holder.itemView.setOnClickListener {
            // Pass problem data to ProblemDetailFragment using bundle
            val bundle = Bundle().apply {
                putString("problemId", problem.key)
                putString("restaurantName", problem.title)
                putString("userEmail", problem.userEmail)
                putString("imageUrl", problem.imageUrl)
                putString("description", problem.description)
                putString("dateStarted", problem.dateStarted)
                putString("ageOfRestaurant", problem.ageOfRestaurant)
                putString("suggestion", problem.suggestion)
                if (isEdit) {
                    putString("expertName", problem.expertName)
                }else{
                    putString("expertName", "")
                }
                putBoolean("isEdit", isEdit)
            }

            // Navigate to ProblemDetailFragment
            val navController = fragmentActivity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navController.navController.navigate(R.id.restaurantPageFragment, bundle)
        }
    }

    override fun getItemCount(): Int {
        return problems.size
    }
}
