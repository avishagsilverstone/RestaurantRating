package com.intro.restaurant.presentation.ui.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.intro.restaurant.R
import com.intro.restaurant.presentation.ui.adapter.RestaurantsAdapter
import com.intro.restaurant.data.db.MyDatabse
import com.intro.restaurant.data.model.RestaurantViewModel
import com.intro.restaurant.data.model.RestaurantViewModelFactory

class FavoriteListFragment : Fragment() {
    private lateinit var viewModel: RestaurantViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_restaurant_list, container, false)
        initializeUI(rootView)
        setupViewModel()
        return rootView
    }

    private fun initializeUI(rootView: View) {
        recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 1)
            adapter = RestaurantsAdapter(requireActivity(), mutableListOf(), false)
        }
        val addButton: Button = rootView.findViewById(R.id.add_button)
        addButton.visibility = View.GONE

        rootView.findViewById<TextView>(R.id.titleTextView).text = "Favorite Restaurants"

    }

    private fun setupViewModel() {
        val db = MyDatabse.getInstance(requireContext().applicationContext)
        val factory = RestaurantViewModelFactory(db.restaurantDao())
        viewModel = ViewModelProvider(this, factory).get(RestaurantViewModel::class.java)

        viewModel.allRestaurants.observe(viewLifecycleOwner, Observer { restaurants ->
            (recyclerView.adapter as RestaurantsAdapter).restaurants = restaurants
            recyclerView.adapter?.notifyDataSetChanged()
        })

        if (isOnline()) {
            FirebaseAuth.getInstance().currentUser?.email?.let {
                viewModel.fetchFavouriteRestaurantsForUserEmail(
                    it
                )
            }
        } else {
            Toast.makeText(context, "Offline: Displaying cached restaurants", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
