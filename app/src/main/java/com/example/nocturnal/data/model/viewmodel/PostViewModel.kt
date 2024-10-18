import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.nocturnal.data.FirestoreRepository
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
class PostViewModel : ViewModel() {
    private val repository = FirestoreRepository()

    fun storePost(mediaUri: String, timestamp: Date, callback: (Boolean) -> Unit) {
        // Create a reference to Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference

        // Create a file path (you can customize this)
        val filePath = "images/IMG_${System.currentTimeMillis()}.jpg" // Or .mp4 for videos
        val fileRef = storageRef.child(filePath)

        // Upload the file
        val uri = Uri.parse(mediaUri)
        fileRef.putFile(uri)
            .addOnSuccessListener {
                // File uploaded successfully, now store the media link in Firestore
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    repository.storePost(downloadUrl.toString(), timestamp)
                    callback(true)
                }.addOnFailureListener {
                    // Handle the error when fetching download URL
                    callback(false)
                }
            }
            .addOnFailureListener {
                // Handle any errors during upload
                callback(false)
            }
    }
}
