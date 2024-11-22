import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nocturnal.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.Date

class PostViewModel : ViewModel() {

    private val repository = FirestoreRepository()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun storePost(mediaUri: String, timestamp: Date, barID: String, onSuccess: (postId: String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val filePath = "images/${auth.currentUser?.uid}/IMG_${System.currentTimeMillis()}.jpg"
        val fileRef = storageRef.child(filePath)

        // Upload the file
        val uri = Uri.parse(mediaUri)
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    auth.currentUser?.let { currentUser ->
                        repository.storePost(downloadUrl.toString(), timestamp, currentUser.uid, barID, onSuccess, onFailure)
                    }
                }.addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }
}
