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

class ImagePreviewFragment : Fragment() {

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
        postButton.setOnClickListener {
            saveMediaToFirestore(imageUri)
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Observe location updates and fetch the nearest bar based on location
        locationService.locationLiveData.observe(viewLifecycleOwner) { userLocation ->
            userLocation?.let {
                barListViewModel.fetchNearestBar(it)
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
}

