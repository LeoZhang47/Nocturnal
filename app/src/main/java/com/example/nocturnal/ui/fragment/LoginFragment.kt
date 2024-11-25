package com.example.nocturnal.ui.fragment

import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nocturnal.databinding.FragmentLoginBinding
import com.example.nocturnal.data.model.viewmodel.UserViewModel
import androidx.fragment.app.viewModels
import com.example.nocturnal.ui.activity.CameraActivity
import android.widget.Toast
import com.example.nocturnal.R


class LoginFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText = binding.username
        val passwordEditText = binding.password
        val loginButton = binding.login
        loginButton.isEnabled = true


        loginButton.setOnClickListener {
            val email = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Check if email or password is empty
            if (email.isEmpty()) {
                usernameEditText.error = "Email cannot be empty"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                passwordEditText.error = "Password cannot be empty"
                return@setOnClickListener
            }

            // Proceed with login if inputs are valid
            userViewModel.loginUser(email, password) { isSuccess, errorMessage ->
                if (isSuccess) {
                    // Navigate to CameraActivity
                    val intent = Intent(activity, CameraActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.login_failed_print_error_msg, errorMessage),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        binding.switchToSignup.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
