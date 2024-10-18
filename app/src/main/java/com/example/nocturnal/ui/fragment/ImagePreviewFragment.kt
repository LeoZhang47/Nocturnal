package com.example.nocturnal.ui.fragment

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.nocturnal.R
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class ImagePreviewFragment : Fragment() {

    private lateinit var postViewModel: PostViewModel

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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageUri = arguments?.getString(ARG_IMAGE_URI)
        imageUri?.let {
            val image_view: ImageView = view.findViewById(R.id.image_view)
            image_view.setImageURI(Uri.parse(it))
        }
        val postButton: Button = view.findViewById(R.id.post_button)
        postButton.setOnClickListener {
            saveMediaToFirestore(imageUri)
            requireActivity().supportFragmentManager.popBackStack()
        }

    }
    private fun saveMediaToFirestore(imageUri: String?) {
        imageUri?.let {
            val mediaUri = Uri.parse(it)
            val timestamp = Date() // Get current timestamp
            postViewModel.storePost(mediaUri.toString(), timestamp)


        }
    }
}
