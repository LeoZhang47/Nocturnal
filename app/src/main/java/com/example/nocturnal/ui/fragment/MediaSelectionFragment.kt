package com.example.nocturnal.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.nocturnal.R
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
        val picture_button: Button = view.findViewById(R.id.picture_button)
        val video_button: Button = view.findViewById(R.id.video_button)

        picture_button.setOnClickListener {
            (activity as CameraActivity).checkCameraPermission("image")
        }

        video_button.setOnClickListener {
            // Handle video recording if needed
        }
    }
}
