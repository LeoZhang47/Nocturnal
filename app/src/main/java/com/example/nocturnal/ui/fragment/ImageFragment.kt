import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.graphics.BitmapFactory
import android.widget.Toast
import com.example.nocturnal.R
import java.io.IOException

class ImageFragment : Fragment() {

    private var mediaUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mediaUri = it.getParcelable(ARG_MEDIA_URI)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_image, container, false)

        // Find ImageView and set the image
        val imageView: ImageView = view.findViewById(R.id.imageView)
        mediaUri?.let {
            loadImageFromUri(it, imageView)
        } ?: run {
            // Show an error if mediaUri is null
            Toast.makeText(context, "Error loading image", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadImageFromUri(uri: Uri, imageView: ImageView) {
        try {
            val bitmap = BitmapFactory.decodeStream(requireContext().contentResolver.openInputStream(uri))
            imageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error loading image from URI", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_MEDIA_URI = "mediaUri"

        // Use this method to create a new instance of ImageFragment
        @JvmStatic
        fun newInstance(uri: Uri) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MEDIA_URI, uri)
                }
            }
    }
}
