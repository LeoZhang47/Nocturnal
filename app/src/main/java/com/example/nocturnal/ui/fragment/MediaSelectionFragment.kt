package com.example.nocturnal.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.nocturnal.R
import com.example.nocturnal.ui.activity.BarListActivity
import com.example.nocturnal.ui.activity.CameraActivity

class MediaSelectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_media_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find buttons from the layout
        val pictureButton: Button = view.findViewById(R.id.picture_button)
        val videoButton: Button = view.findViewById(R.id.video_button)
        val viewBarsButton: Button = view.findViewById(R.id.btn_view_bars) // New View Bars button

        // Set click listener for picture button
        pictureButton.setOnClickListener {
            (activity as CameraActivity).checkCameraPermission("image")
        }

        // Set click listener for video button
        videoButton.setOnClickListener {
            // Handle video recording logic
        }

        // Set click listener for "View Bars" button
        viewBarsButton.setOnClickListener {
            // Navigate to BarListActivity when clicked
            val intent = Intent(requireContext(), BarListActivity::class.java)
            startActivity(intent)
        }
    }
}
