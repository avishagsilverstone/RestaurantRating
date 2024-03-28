package com.intro.restaurant.presentation.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.intro.restaurant.R
import com.intro.restaurant.data.db.MyDatabse
import com.intro.restaurant.data.model.RestaurantModel
import com.squareup.picasso.Picasso

class AddRestaurantFragment : Fragment(), OnMapReadyCallback {
    private lateinit var restaurantTitleEditText: EditText
    private lateinit var restaurantDescriptionEditText: EditText
    private lateinit var restaurantImageView: ImageView
    private lateinit var foodtypeEditText: EditText
    private lateinit var map_edittext: EditText
    private lateinit var addRestaurantButton: Button
    private lateinit var cancelButton: Button
    private lateinit var map: GoogleMap
    private var selectedImageUri: Uri? = null
    private var selectedLocation: LatLng? = null

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var localDb: MyDatabse
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { result ->
        if (result != null) {
            try {
                result?.let {
                    Picasso.get().load(it).into(restaurantImageView, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            selectedImageUri = it
                        }
                        override fun onError(e: Exception?) {
                            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                            e?.printStackTrace()
                        }
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val AUTOCOMPLETE_REQUEST_CODE = 1

    private fun selectAddress() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .build(requireContext())
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        localDb = MyDatabse.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_add_restaurant, container, false)
        initializeViews(rootView)
        setupMapFragment()
        setupListeners()
        return rootView
    }

    private fun initializeViews(rootView: View) {
        restaurantTitleEditText = rootView.findViewById(R.id.restaurantname_textview)
        restaurantDescriptionEditText = rootView.findViewById(R.id.restaurantdescription_textview)
        restaurantImageView = rootView.findViewById(R.id.restaurantImage)
        foodtypeEditText = rootView.findViewById(R.id.date_edittext)
        map_edittext = rootView.findViewById(R.id.map_edittext)
        addRestaurantButton = rootView.findViewById(R.id.add_button)
        cancelButton = rootView.findViewById(R.id.cancel_button)

    }

    private fun setupMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapsFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun setupListeners() {
        restaurantImageView.setOnClickListener { selectImage() }
        addRestaurantButton.setOnClickListener { uploadRestaurantData() }
        cancelButton.setOnClickListener { NavHostFragment.findNavController(this).popBackStack() }
        map_edittext.setOnClickListener { selectAddress() }
    }

    private fun selectImage() {
        imagePickerLauncher.launch("image/*")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    private fun uploadRestaurantData() {
        val name = restaurantTitleEditText.text.toString().trim()
        val menu = restaurantDescriptionEditText.text.toString().trim()
        val address = map_edittext.text.toString().trim()
        val foodtype = foodtypeEditText.text.toString().trim()

        if (name.isEmpty() || menu.isEmpty() || selectedImageUri == null || selectedLocation == null || address.isEmpty() || foodtype.isEmpty()) {
            Toast.makeText(requireContext(), "All fields and location must be filled", Toast.LENGTH_SHORT).show()
            return
        }
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Adding restaurant...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        // First, upload the image to Firebase Storage
        val imageRef: StorageReference = storage.reference.child("restaurant_images/${name}.jpg")
        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        // Then, save restaurant data including the image URL to Firestore
                        val restaurant = hashMapOf<String, Any>()
                        restaurant["name"] = name
                        restaurant["menu"] = menu
                        restaurant["imageUrl"] = uri.toString()
                        restaurant["latitude"] = selectedLocation!!.latitude
                        restaurant["longitude"] = selectedLocation!!.longitude
                        restaurant["userEmail"] = FirebaseAuth.getInstance().currentUser?.email!!
                        restaurant["address"] = address
                        restaurant["foodtype"] = foodtype
                        restaurant["review"] = ""
                        restaurant["favourite"] = false

                        firestore.collection("Restaurants")
                            .add(restaurant)
                            .addOnSuccessListener { documentReference ->
                                progressDialog.dismiss()
                                Toast.makeText(requireContext(), "Restaurant added successfully", Toast.LENGTH_SHORT).show()
                                NavHostFragment.findNavController(this).popBackStack()

                                // Optionally, you can also save this restaurant data to Room database for offline access
                                saveRestaurantToLocalDatabase(
                                    RestaurantModel(
                                        documentReference.id,
                                        FirebaseAuth.getInstance().currentUser?.email!!,
                                        uri.toString(),
                                        name,
                                        menu,
                                        selectedLocation!!.latitude,
                                        selectedLocation!!.longitude,
                                        address,
                                        foodtype,
                                        "",
                                        false
                                    )
                                )
                            }
                            .addOnFailureListener { e ->
                                progressDialog.dismiss()
                                Toast.makeText(requireContext(), "Failed to add restaurant", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveRestaurantToLocalDatabase(restaurant: RestaurantModel) {
        Thread { localDb.restaurantDao().insert(restaurant) }.start()
    }
}
