package com.intro.restaurant.presentation.ui.fragments

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
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
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RestaurantDetailFragment : Fragment(), OnMapReadyCallback , DatePickerDialog.OnDateSetListener {
    private lateinit var map: GoogleMap
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var myDB: MyDatabse
    private lateinit var restaurantDao: RestaurantDao
    private lateinit var currentRestaurant: RestaurantModel
    private lateinit var imageView: ImageView
    private lateinit var restaurantNameEditText: EditText
    private lateinit var textRestaurantDescription: EditText
    private lateinit var textDateStarted: EditText
    private lateinit var textAgeOfRestaurant: EditText
    private lateinit var textSuggestion: EditText
    private lateinit var suggestionTextView: TextView
    private lateinit var saveEdits: Button
    private lateinit var deleteButton: Button
    private var selectedImageUri: Uri? = null
    private var selectedLocation: LatLng? = null
    private lateinit var mAuth: FirebaseAuth
    companion object {
        private const val REQUEST_CODE_GALLERY = 1
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        val bundle = arguments
        bundle?.let {
            setCurrentRestaurant(bundle)
            restaurantNameEditText.setText(currentRestaurant.title)
            textAgeOfRestaurant.setText(currentRestaurant.ageOfRestaurant)
            suggestionTextView.setText(currentRestaurant.suggestion)
            suggestionTextView.setText(currentRestaurant.expertName)
            textDateStarted.setText(currentRestaurant.dateStarted)
            textRestaurantDescription.setText(currentRestaurant.description)
            Picasso.get().load(currentRestaurant.imageUrl).into(imageView)
            val hotelPosition = LatLng(currentRestaurant.latitude, currentRestaurant.longitude)
            val marker = map.addMarker(MarkerOptions().position(hotelPosition).title(currentRestaurant.title).snippet(currentRestaurant.key))
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
        restaurantDao = myDB.restaurantProblemDao()
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
        textRestaurantDescription = rootView.findViewById(R.id.description_edittext)
        textDateStarted = rootView.findViewById(R.id.date_edittext)
        textAgeOfRestaurant = rootView.findViewById(R.id.age_edittext)
        textSuggestion = rootView.findViewById(R.id.suggestion_edittext)
        suggestionTextView = rootView.findViewById(R.id.suggestion_label)
        saveEdits = rootView.findViewById(R.id.save_button)
        deleteButton = rootView.findViewById(R.id.delete_button)
        imageView = rootView.findViewById(R.id.restaurantImage)

        imageView.setOnClickListener { openGallery() }
        textDateStarted.setOnClickListener {
            showDatePickerDialog()
        }
        saveEdits.setOnClickListener {
            val newRestaurantName = restaurantNameEditText.text.toString()
            val newDescription = textRestaurantDescription.text.toString()
            val newDateStarted = textDateStarted.text.toString()
            val newAgeOfRestaurant = textAgeOfRestaurant.text.toString()
            val user = mAuth.currentUser
            var newExpertName = "";
            user?.let {
                newExpertName = user.displayName.toString()
            }
            val newSuggestion = suggestionTextView.text.toString() + "\n" + newExpertName+":" +textSuggestion.text.toString()
            selectedImageUri?.let { uri ->
                val storageRef = FirebaseStorage.getInstance().getReference().child("restaurant_images/${newRestaurantName}")
                storageRef.putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        storageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                            currentRestaurant.imageUrl = imageUrl.toString()
                            firebaseFirestore.collection("Restaurants").document(currentRestaurant.key.toString())
                                .update("name", newRestaurantName, "description", newDescription,"imageUrl", imageUrl.toString(),"latitude", selectedLocation!!.latitude,"longitude", selectedLocation!!.longitude,"dateStarted", newDateStarted, "ageOfRestaurant", newAgeOfRestaurant, "suggestion", newSuggestion, "expertName", newExpertName)

                                .addOnSuccessListener {
                                    currentRestaurant.apply {
                                        title = newRestaurantName
                                        description = newDescription
                                        dateStarted = newDateStarted
                                        ageOfRestaurant = newAgeOfRestaurant
                                        suggestion = newSuggestion
                                        expertName = newExpertName
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
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), this, year, month, dayOfMonth)
        datePickerDialog.show()
    }
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }.time)
        textDateStarted.setText(selectedDate)
    }
    private fun openGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun setCurrentRestaurant(bundle: Bundle) {
        val restaurantId = bundle.getString("restaurantId")?: ""
        val userEmail = bundle.getString("userEmail")?: ""
        val restaurantName = bundle.getString("restaurantName")?: ""
        val imageUrl = bundle.getString("imageUrl")?: ""
        selectedImageUri  =  Uri.parse(imageUrl)
        val description = bundle.getString("description")?: ""
        val latitude = bundle.getDouble("latitude")
        val longitude = bundle.getDouble("longitude")
        val dateStarted = bundle.getString("dateStarted")?: ""
        val ageOfRestaurant = bundle.getString("ageOfRestaurant")?: ""
        val suggestion = bundle.getString("suggestion")?: ""
        val expertName = bundle.getString("expertName") ?: ""

        currentRestaurant = RestaurantModel(restaurantId!!, userEmail!!,  imageUrl!!, restaurantName!!, description!!, latitude, longitude, dateStarted!!, ageOfRestaurant!!, suggestion, expertName)
        if (latitude != 0.0 && longitude != 0.0) {
            val restaurantPosition = LatLng(latitude, longitude)
            val marker = map.addMarker(MarkerOptions().position(restaurantPosition).title(restaurantName).snippet(restaurantId))

        }
        val isEdit = bundle.getBoolean("isEdit")
        if (!isEdit) {
            restaurantNameEditText.isEnabled = false
            textRestaurantDescription.isEnabled = false
            textDateStarted.isEnabled = false
            textAgeOfRestaurant.isEnabled = false
            textSuggestion.isEnabled = false
            saveEdits.visibility = View.GONE
            deleteButton.visibility = View.GONE
            imageView.isEnabled = false
        }else{
            suggestionTextView.visibility = View.GONE
            map.setOnMapClickListener { latLng ->
                selectedLocation = latLng
                map.clear()
                map.addMarker(MarkerOptions().position(latLng))
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        TODO("Not yet implemented")
    }

}
