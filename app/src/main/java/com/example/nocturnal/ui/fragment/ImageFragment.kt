import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.example.nocturnal.R
import java.io.File
import java.io.IOException
import kotlin.math.max


/**
 * Fragment for showing and capturing images.
 *
 * Now includes ActivityResult launchers.
 *
 * Created by adamcchampion on 2017/08/12.
 */
@Keep
class ImageFragment : Fragment(), View.OnClickListener {
    private lateinit var mImageView: ImageView
    private lateinit var mImageFilePath: String
    private val mBitmapLiveData = MutableLiveData<Bitmap?>()
    private var mBitmap: Bitmap? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_image, container, false)
        mImageView = v.findViewById(R.id.imageView)
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        mImageFilePath = imageDir!!.path + File.separator + "sample_image.jpg"
    }


    /**
     * Decodes the Bitmap captured by the Camera, and returns the Bitmap. Adapted from Chapter 16
     * in the "Big Nerd Ranch Guide to Android Development", fourth edition.
     *
     * @param selectedFileUri Uri corresponding to the Bitmap to decode
     * @return The scaled Bitmap for the ImageView
     */
    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        var image: Bitmap? = null
        var parcelFileDescriptor: ParcelFileDescriptor? = null

        try {
            val activity: Activity = requireActivity()
            parcelFileDescriptor = activity.contentResolver.openFileDescriptor(selectedFileUri, "r")
            if (parcelFileDescriptor != null) {
                val fileDescriptor = parcelFileDescriptor.fileDescriptor

                // Get the bounds
                val optionsForBounds = BitmapFactory.Options()
                optionsForBounds.inJustDecodeBounds = true

                val dstWidth = mImageView.width
                val dstHeight = mImageView.height

                BitmapFactory.decodeFileDescriptor(
                    fileDescriptor,
                    mImageView.drawable.bounds,
                    optionsForBounds
                )

                val srcWidth = optionsForBounds.outWidth.toFloat()
                val srcHeight = optionsForBounds.outHeight.toFloat()

                var inSampleSize = 1

                if (srcWidth > dstWidth || srcHeight > dstHeight) {
                    val widthScale = srcWidth / dstWidth
                    val heightScale = srcHeight / dstHeight

                    val sampleScale =
                        max(widthScale.toDouble(), heightScale.toDouble()).toFloat()
                    inSampleSize = Math.round(sampleScale)
                }

                val actualOptions = BitmapFactory.Options()
                actualOptions.inSampleSize = inSampleSize

                image = BitmapFactory.decodeFileDescriptor(
                    fileDescriptor,
                    mImageView.drawable.bounds,
                    actualOptions
                )
                // largeBitmap.recycle();
                parcelFileDescriptor.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }

}
