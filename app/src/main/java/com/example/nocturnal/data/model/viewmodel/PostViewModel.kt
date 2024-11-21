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
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun storePost(
        mediaUri: String,
        timestamp: Date,
        barID: String,
        onSuccess: (postId: String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure(Exception("User not authenticated"))
            return
        }

        val filePath = "images/${currentUser.uid}/IMG_${System.currentTimeMillis()}.jpg"
        val fileRef = storage.reference.child(filePath)

        val uri = Uri.parse(mediaUri)

        // Upload file to Firebase Storage
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Once uploaded, store post data in Firestore
                    viewModelScope.launch {
                        try {
                            val postId = repository.storePost(
                                media = downloadUrl.toString(),
                                timestamp = timestamp,
                                uid = currentUser.uid,
                                barID = barID
                            )
                            // Update the bar's postIDs field
                            repository.updateBarPostIds(barId = barID, postId = postId)
                            onSuccess(postId)
                        } catch (e: Exception) {
                            onFailure(e)
                        }
                    }
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}
