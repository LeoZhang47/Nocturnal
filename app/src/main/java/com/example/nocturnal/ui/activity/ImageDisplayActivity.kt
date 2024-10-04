
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.example.nocturnal.ui.SingleFragmentActivity

/**
 * Activity for showing and hosting images.
 *
 * Created by adamcchampion on 2017/08/12.
 */
@Keep
class ImageDisplayActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment {
        return ImageFragment()
    }
}
