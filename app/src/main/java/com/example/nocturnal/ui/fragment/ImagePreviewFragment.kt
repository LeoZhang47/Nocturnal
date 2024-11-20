package com.example.nocturnal.ui.fragment

import LocationService
import PostViewModel
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nocturnal.R
import com.example.nocturnal.data.Bar
import com.example.nocturnal.data.model.viewmodel.BarListViewModel
import java.util.Date
import android.util.Log
import androidx.fragment.app.DialogFragment
import kotlin.math.*
import com.example.nocturnal.data.model.viewmodel.CameraViewModel
import androidx.fragment.app.activityViewModels
import com.example.nocturnal.data.model.distanceTo
import com.example.nocturnal.data.model.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import android.graphics.Color

class ImagePreviewFragment : DialogFragment() {

    private val cameraViewModel: CameraViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var postViewModel: PostViewModel
    private lateinit var barListViewModel: BarListViewModel
    private lateinit var locationService: LocationService

    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val ARG_IMAGE_URI = "image_uri"

        fun newInstance(imageUri: String): ImagePreviewFragment {
            val fragment = ImagePreviewFragment()
            val args = Bundle()
            args.putString(ARG_IMAGE_URI, imageUri)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postViewModel = ViewModelProvider(this)[PostViewModel::class.java] // Initialize ViewModel
        barListViewModel = ViewModelProvider(this, BarListViewModel.Factory)[BarListViewModel::class.java]
        locationService = LocationService(requireContext())
        locationService.startLocationUpdates()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_preview, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationService.stopLocationUpdates()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUri = arguments?.getString(ARG_IMAGE_URI)
        imageUri?.let {
            val imageView: ImageView = view.findViewById(R.id.image_view)
            imageView.setImageURI(Uri.parse(it))
        }

        val postButton: Button = view.findViewById(R.id.post_button)
        val cancelButton: Button = view.findViewById(R.id.cancel_button)

        // Observe location updates and call fetchNearestBar
        locationService.locationLiveData.observe(viewLifecycleOwner) { userLocation ->
            userLocation?.let {
                // Fetch the nearest bar based on user location
                barListViewModel.fetchNearestBar(it)
            }
        }

        // Observe isWithinRange to update the button color
        cameraViewModel.isWithinRange.observe(viewLifecycleOwner) { isWithinRange ->
            if (isWithinRange == true) {
                postButton.setBackgroundColor(Color.parseColor("#16ad02"))
            } else {
                //postButton.setBackgroundColor(requireContext().getColor(R.color.gray)) // Replace with your default color resource
            }
        }

        // Set range
        val range = 0.1

        // Observe nearestBar to determine if the user is within 0.1 miles
        barListViewModel.nearestBar.observe(viewLifecycleOwner) { nearestBar ->
            nearestBar?.location?.let { barLocation ->
                val userLocation = locationService.locationLiveData.value
                userLocation?.let { location ->
                    val barPoint = Point.fromLngLat(barLocation.longitude, barLocation.latitude)
                    val distance = location.distanceTo(barPoint)

                    // Update isWithinRange in SharedViewModel based on the distance
                    val isWithinRange = distance <= range
                    cameraViewModel.setWithinRange(isWithinRange)

                    Log.d("NearestBar", "Nearest Bar: ${nearestBar.name}, Distance: $distance miles")
                }
            } ?: run {
                cameraViewModel.setWithinRange(false)  // No bar nearby, so set to false
                Log.d("NearestBar", "No bar found nearby")
            }
        }

        // Set up postButton click listener
        postButton.setOnClickListener {
            if (cameraViewModel.isWithinRange.value == true) {
                // If within range, proceed with posting
                saveMediaToFirestore(imageUri)
                userViewModel.incrementUserScore(
                    incrementBy = 1,  // or any other increment value
                    onSuccess = {
                        // Handle successful score increment, e.g., show a success message
                    },
                    onFailure = { errorMessage ->
                        // Handle failure, e.g., show an error message
                        Log.e("UserViewModel", "Error incrementing score: $errorMessage")
                    }
                )

                requireActivity().supportFragmentManager.popBackStack()
                // Close the ImagePreviewFragment
                dismiss()
            } else {
                // If not within range, show a toast message
                Toast.makeText(
                    requireContext(),
                    getString(R.string.bar_out_of_range, range),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            // Customize dialog properties (e.g., full-screen, cancelable, etc.)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            isCancelable = true
            setCanceledOnTouchOutside(true)
        }
    }

    private fun saveMediaToFirestore(imageUri: String?) {
        imageUri?.let {
            val mediaUri = Uri.parse(it)
            val timestamp = Date()
            val currentNearestBar = barListViewModel.nearestBar.value
            if (currentNearestBar != null) {
                processPostWithBar(currentNearestBar, mediaUri, timestamp)
                // TODO! This toast should not go here:
                Toast.makeText(requireActivity(), getString(R.string.post_added_confirmation, currentNearestBar.name), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireActivity(), getString(R.string.no_nearby_bar), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processPostWithBar(nearestBar: Bar, mediaUri: Uri, timestamp: Date) {
        nearestBar.id?.let { barId ->
            postViewModel.storePost(
                mediaUri.toString(),
                timestamp,
                barId,
                onSuccess = { postId ->
                    updateBarWithPostId(barId, postId)
                    // Toast.makeText(requireActivity(), "Post added to bar ${nearestBar.name}", Toast.LENGTH_SHORT).show()
                },
                onFailure = { exception ->
//                    Toast.makeText(
//                        requireActivity(),
//                        "Failed to store post: ${exception.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            )
        }
    }

    private fun updateBarWithPostId(barId: String, postId: String) {
        barListViewModel.repository.updateBarPostIds(barId, postId) { success, exception ->
            if (success) {
                // Toast.makeText(requireActivity(), "Post added to bar", Toast.LENGTH_SHORT).show()
            } else {
                // Toast.makeText(requireActivity(), "Failed to update bar: ${exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

