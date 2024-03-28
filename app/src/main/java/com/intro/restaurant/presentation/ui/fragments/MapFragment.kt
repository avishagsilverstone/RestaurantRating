package com.intro.restaurant.presentation.ui.fragments

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.intro.restaurant.R
import java.io.IOException

class MapFragment : Fragment() {

    private lateinit var map: GoogleMap

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        setupMapListeners()
        loadRestaurants()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        return view
    }

    private fun setupMapListeners() {
        map.setOnMarkerClickListener { marker ->
            val bundle = Bundle()
            val document = marker.tag as? QueryDocumentSnapshot
            document?.let {
                val restaurantId = document.id
                val name = document.getString("name") ?: ""
                val userEmail = document.getString("userEmail") ?: ""
                val imageUrl = document.getString("imageUrl") ?: ""
                val menu = document.getString("menu") ?: ""
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0


                val address = document.getString("address") ?: ""
                val foodtype = document.getString("foodtype") ?: ""
                val review = document.getString("review") ?: ""
                val favourite = document.getBoolean("favourite") ?: false
                bundle.putString("restaurantId", restaurantId)
                bundle.putString("name", name)
                bundle.putString("userEmail", userEmail)
                bundle.putString("imageUrl", imageUrl)
                bundle.putString("menu", menu)
                bundle.putDouble("latitude", latitude)
                bundle.putDouble("longitude", longitude)

                bundle.putString("address", address)
                bundle.putString("foodtype", foodtype)
                bundle.putString("review", review)
                bundle.putBoolean("favourite", favourite)
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_mapsFragment_to_restaurantPageFragment, bundle)
            }
            false
        }
    }

    private fun loadRestaurants() {
        val db = FirebaseFirestore.getInstance()

        db.collection("Restaurants").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                try {
                    for (document in task.result!!) {
                        val restaurantId = document.id
                        val name = document.getString("name") ?: ""
                        val latitude = document.getDouble("latitude") ?: 0.0
                        val longitude = document.getDouble("longitude") ?: 0.0




                        if (latitude != 0.0 && longitude != 0.0) {
                            val restaurantPosition = LatLng(latitude, longitude)
                            val marker = map.addMarker(MarkerOptions().position(restaurantPosition).title(name).snippet(restaurantId))
                            marker?.tag = document
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error loading restaurants", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Failed to fetch restaurants", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapsFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)
    }
}
