import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nocturnal.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.Date
class PostViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    fun storePost(mediaUri: String, timestamp: Date /*, onSuccess: () -> Unit, onFailure: (Exception) -> Unit*/) {
        // Create a reference to Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference

        // Create a file path (you can customize this)
        val filePath = "images/${auth.currentUser?.uid}/IMG_${System.currentTimeMillis()}.jpg" // Or .mp4 for videos
        val fileRef = storageRef.child(filePath)

        // Upload the file
        val uri = Uri.parse(mediaUri)
        fileRef.putFile(uri)
            .addOnSuccessListener {
                // File uploaded successfully, now store the media link in Firestore
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    auth.currentUser?.let { it1 -> repository.storePost(downloadUrl.toString(), timestamp, it1.uid /*, onSuccess, onFailure*/) }
                }.addOnFailureListener {
                }
            }
            .addOnFailureListener {
                // Handle any errors during upload
            }
    }
}
