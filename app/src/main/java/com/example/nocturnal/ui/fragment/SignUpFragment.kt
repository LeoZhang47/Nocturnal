package com.example.nocturnal.ui.fragment

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.nocturnal.R
import com.example.nocturnal.databinding.FragmentSignUpBinding
import com.example.nocturnal.data.model.viewmodel.UserViewModel
import com.example.nocturnal.ui.activity.CameraActivity

class SignUpFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailEditText = binding.email
        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val confirmPasswordEditText = binding.confirmPassword
        val registerButton = binding.register

        // Detect orientation and update layout or UI elements accordingly
        adjustLayoutForOrientation(resources.configuration.orientation)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (password == confirmPassword) {
                userViewModel.registerUser(email, password) { isSuccess, errorMessage, uid ->
                    if (isSuccess && uid != null) {
                        userViewModel.storeUsername(uid, username)
                        userViewModel.storeScore(uid, 0)
                        Toast.makeText(context, R.string.registration_successful, Toast.LENGTH_LONG).show()

                        // Navigate to CameraActivity
                        val intent = Intent(activity, CameraActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(context, getString(R.string.registration_failed_print_error_msg, errorMessage), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, R.string.password_mismatch, Toast.LENGTH_LONG).show()
            }
        }

        binding.switchToLogin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun adjustLayoutForOrientation(orientation: Int) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Make all text smaller in landscape mode
            binding.signupMessage.visibility = View.GONE
            binding.email.layoutParams = (binding.email.layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = 4 // Set margin to 4dp in landscape mode
            }
            binding.email.requestLayout()
            //binding.email.setHorizontalBias(0.0f) // Set horizontal bias
            binding.signupMessage.textSize = 16f
            binding.email.textSize = 14f
            binding.username.textSize = 14f
            binding.password.textSize = 14f
            binding.confirmPassword.textSize = 14f
            binding.register.textSize = 14f
            binding.switchToLogin.textSize = 12f
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Reset text sizes for portrait mode
            binding.signupMessage.textSize = 20f
            binding.email.textSize = 16f
            binding.username.textSize = 16f
            binding.password.textSize = 16f
            binding.confirmPassword.textSize = 16f
            binding.register.textSize = 16f
            binding.switchToLogin.textSize = 14f
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjustLayoutForOrientation(newConfig.orientation)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
