package com.example.nocturnal.ui.fragment

import LocationService
import PostViewModel
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
import kotlin.math.*
import com.example.nocturnal.data.model.viewmodel.CameraViewModel
import androidx.fragment.app.activityViewModels

class ImagePreviewFragment : Fragment() {

    private val cameraViewModel: CameraViewModel by activityViewModels()
    private lateinit var postViewModel: PostViewModel
    private lateinit var barListViewModel: BarListViewModel
    private lateinit var locationService: LocationService

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

        // Observe location updates and call fetchNearestBar
        locationService.locationLiveData.observe(viewLifecycleOwner) { userLocation ->
            userLocation?.let {
                // Fetch the nearest bar based on user location
                barListViewModel.fetchNearestBar(it)
            }
        }

        // Observe nearestBar to determine if the user is within 0.3 miles
        barListViewModel.nearestBar.observe(viewLifecycleOwner) { nearestBar ->
            nearestBar?.location?.let { barLocation ->
                val userLocation = locationService.locationLiveData.value
                userLocation?.let { location ->
                    val distance = calculateDistance(
                        location.latitude(),
                        location.longitude(),
                        barLocation.latitude,
                        barLocation.longitude
                    )

                    // Update isWithinRange in SharedViewModel based on the distance
                    val isWithinRange = distance <= 0.5
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
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                // If not within range, show a toast message
                Toast.makeText(
                    requireContext(),
                    "You are not within 0.3 miles of a bar",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
                Toast.makeText(requireActivity(), "Post added to bar ${currentNearestBar.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireActivity(), "No nearby bar found", Toast.LENGTH_SHORT).show()
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

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        Log.d("DistanceCalculation", "lat1: $lat1, lon1: $lon1, lat2: $lat2, lon2: $lon2")

        val earthRadiusMiles = 3958.8 // Earth radius in miles
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMiles * c
    }
}

