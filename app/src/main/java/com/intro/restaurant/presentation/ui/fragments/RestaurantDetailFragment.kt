package com.intro.restaurant.presentation.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.intro.restaurant.R
import com.intro.restaurant.data.db.MyDatabse
import com.intro.restaurant.data.db.RestaurantDao
import com.intro.restaurant.data.model.RestaurantModel
import com.squareup.picasso.Picasso
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth

class RestaurantDetailFragment : Fragment(), OnMapReadyCallback  {
    private lateinit var map: GoogleMap
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var myDB: MyDatabse
    private lateinit var restaurantDao: RestaurantDao
    private lateinit var currentRestaurant: RestaurantModel
    private lateinit var imageView: ImageView
    private lateinit var restaurantNameEditText: EditText
    private lateinit var favoriteButton: ImageButton
    private lateinit var textMenu: EditText
    private lateinit var foodType: EditText
    private lateinit var map_edittext: EditText
    private lateinit var textReview: EditText
    private lateinit var reviewTextView: TextView
    private lateinit var saveEdits: Button
    private lateinit var deleteButton: Button
    private var selectedImageUri: Uri? = null
    private var selectedLocation: LatLng? = null
    private lateinit var mAuth: FirebaseAuth
    private var isFavorite: Boolean = false

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        val bundle = arguments
        bundle?.let {
            setCurrentRestaurant(bundle)

            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.heart)
            } else {
                favoriteButton.setImageResource(R.drawable.heart_empty)
            }
            restaurantNameEditText.setText(currentRestaurant.name)
            reviewTextView.setText(currentRestaurant.review)
            foodType.setText(currentRestaurant.foodtype)
            map_edittext.setText(currentRestaurant.address)
            textMenu.setText(currentRestaurant.menu)
            Picasso.get().load(currentRestaurant.imageUrl).into(imageView)
            val hotelPosition = LatLng(currentRestaurant.latitude, currentRestaurant.longitude)
            val marker = map.addMarker(MarkerOptions().position(hotelPosition).title(currentRestaurant.name).snippet(currentRestaurant.key))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(hotelPosition, 15f))
        }
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
        result?.let {
            try {
                Picasso.get().load(it).into(imageView)
                selectedImageUri = it
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseFirestore = FirebaseFirestore.getInstance()
        myDB = Room.databaseBuilder(requireContext().applicationContext,
            MyDatabse::class.java, "restaurant_database").allowMainThreadQueries().build()
        restaurantDao = myDB.restaurantDao()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val callback1 = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapsFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback1)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_restaurant_detail, container, false)
        mAuth = FirebaseAuth.getInstance()
        restaurantNameEditText = rootView.findViewById(R.id.name_edittext)
        textMenu = rootView.findViewById(R.id.description_edittext)
        foodType = rootView.findViewById(R.id.date_edittext)
        map_edittext = rootView.findViewById(R.id.map_edittext)
        textReview = rootView.findViewById(R.id.review_edittext)
        reviewTextView = rootView.findViewById(R.id.review_label)
        saveEdits = rootView.findViewById(R.id.save_button)
        deleteButton = rootView.findViewById(R.id.delete_button)
        imageView = rootView.findViewById(R.id.restaurantImage)

        imageView.setOnClickListener { openGallery() }
        favoriteButton = rootView.findViewById(R.id.favorite_button)
        favoriteButton.setOnClickListener {
            isFavorite = !isFavorite
            val favoriteButton: ImageButton = rootView.findViewById(R.id.favorite_button)
            if (isFavorite) {
                favoriteButton.setImageResource(R.drawable.heart)
            } else {
                favoriteButton.setImageResource(R.drawable.heart_empty)
            }
            firebaseFirestore.collection("Restaurants").document(currentRestaurant.key.toString())
                .update("favourite", isFavorite)

                .addOnSuccessListener {
                    currentRestaurant.apply {
                        favourite = isFavorite
                    }
                    restaurantDao.update(currentRestaurant)
                    Toast.makeText(context, "Restaurant updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { Toast.makeText(context, "Error updating restaurant", Toast.LENGTH_SHORT).show() }

        }

        saveEdits.setOnClickListener {
            val newRestaurantName = restaurantNameEditText.text.toString()
            val newMenu = textMenu.text.toString()
            val newFoodType = foodType.text.toString()
            val user = mAuth.currentUser
            var newOwnerName = "";
            user?.let {
                newOwnerName = user.displayName.toString()
            }
            val newReview = reviewTextView.text.toString() + "\n" + newOwnerName+":" +textReview.text.toString()
            selectedImageUri?.let { uri ->
                val storageRef = FirebaseStorage.getInstance().getReference().child("restaurant_images/${newRestaurantName}")
                storageRef.putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            currentRestaurant.imageUrl = imageUrl.toString()
                            firebaseFirestore.collection("Restaurants").document(currentRestaurant.key.toString())
                                .update("name", newRestaurantName, "description", newMenu,"imageUrl", imageUrl.toString(),"latitude", selectedLocation!!.latitude,"longitude", selectedLocation!!.longitude,"address", newFoodType, "foodtype", newFoodType, "review", newReview, "favourite", isFavorite)

                                .addOnSuccessListener {
                                    currentRestaurant.apply {
                                        name = newRestaurantName
                                        menu = newMenu
                                        address = map_edittext.text.toString()
                                        foodtype = newFoodType
                                        review = newReview
                                        favourite = isFavorite
                                    }
                                    restaurantDao.update(currentRestaurant)
                                    Toast.makeText(context, "Restaurant updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { Toast.makeText(context, "Error updating restaurant", Toast.LENGTH_SHORT).show() }

                            Toast.makeText(context, "Image updated successfully", Toast.LENGTH_SHORT).show()

                        }
                    }
                    .addOnFailureListener { Toast.makeText(context, "Error uploading image", Toast.LENGTH_SHORT).show() }
            }

        }

        deleteButton.setOnClickListener {
            currentRestaurant.key?.let { restaurantKey ->
                firebaseFirestore.collection("Restaurants").document(restaurantKey)
                    .delete()
                    .addOnSuccessListener {
                        restaurantDao.delete(currentRestaurant)
                        Toast.makeText(context, "Restaurant deleted successfully", Toast.LENGTH_SHORT).show()
                        activity?.onBackPressed()
                    }
                    .addOnFailureListener { Toast.makeText(context, "Error deleting restaurant", Toast.LENGTH_SHORT).show() }
            }
        }

        return rootView
    }
    private fun selectAddress() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())
        startActivityForResult(intent, 11)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11) {
            if (resultCode == Activity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                val address = place.address
                map_edittext.setText(address)
                val latitude = place.latLng?.latitude
                val longitude = place.latLng?.longitude
                if (latitude != null && longitude != null) {
                    selectedLocation = LatLng(latitude, longitude)
                    map.clear()
                    map.addMarker(MarkerOptions().position(selectedLocation!!).title(address))
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation!!, 15f))
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
            }
        }
    }
    private fun openGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun setCurrentRestaurant(bundle: Bundle) {
        val restaurantId = bundle.getString("restaurantId")?: ""
        val userEmail = bundle.getString("userEmail")?: ""
        val name = bundle.getString("name")?: ""
        val imageUrl = bundle.getString("imageUrl")?: ""
        selectedImageUri  =  Uri.parse(imageUrl)
        val menu = bundle.getString("menu")?: ""
        val latitude = bundle.getDouble("latitude")
        val longitude = bundle.getDouble("longitude")
        val address = bundle.getString("address")?: ""
        val foodtype = bundle.getString("foodtype")?: ""
        val review = bundle.getString("review")?: ""
        isFavorite = bundle.getBoolean("favourite") ?: false

        currentRestaurant =

            RestaurantModel(
                key =  restaurantId!!,
                userEmail =userEmail!!,
                imageUrl =imageUrl!!,
                name =name!!,
                menu =menu!!,
                latitude =latitude,
                longitude =longitude,
                address =address!!,
                foodtype =foodtype!!,
                review =review,
                favourite =isFavorite)
        if (latitude != 0.0 && longitude != 0.0) {
            val restaurantPosition = LatLng(latitude, longitude)
            val marker = map.addMarker(MarkerOptions().position(restaurantPosition).title(name).snippet(restaurantId))

        }
        val isEdit = bundle.getBoolean("isEdit")
        if (!isEdit) {
            restaurantNameEditText.isEnabled = false
            textMenu.isEnabled = false
            foodType.isEnabled = false
            textReview.isEnabled = false
            saveEdits.visibility = View.GONE
            deleteButton.visibility = View.GONE
            imageView.isEnabled = false
        }else{
            map_edittext.setOnClickListener { selectAddress() }
            reviewTextView.visibility = View.GONE

        }
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }

}
