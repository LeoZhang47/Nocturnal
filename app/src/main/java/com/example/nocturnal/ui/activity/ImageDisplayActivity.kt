package com.example.nocturnal.ui.activity

import ImageFragment
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.example.nocturnal.ui.SingleFragmentActivity

@Keep
class ImageDisplayActivity : SingleFragmentActivity() {

    override fun createFragment(): Fragment {
        // Get the Uri passed to this activity
        val uri: Uri? = intent.getParcelableExtra(EXTRA_IMAGE_URI)

        // Pass the Uri to the ImageFragment
        return uri?.let {
            ImageFragment.newInstance(it)
        } ?: ImageFragment()
    }

    companion object {
        const val EXTRA_IMAGE_URI = "com.example.nocturnal.ui.EXTRA_IMAGE_URI"

        // Method to launch this activity with the Uri
        fun newIntent(context: Context, imageUri: Uri): Intent {
            return Intent(context, ImageDisplayActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URI, imageUri)
            }
        }
    }
}
