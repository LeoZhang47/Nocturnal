package com.example.nocturnal.ui.signup

import android.content.Intent
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nocturnal.databinding.FragmentSignUpBinding
import com.example.nocturnal.data.model.viewmodel.UserViewModel
import androidx.fragment.app.viewModels
import com.example.nocturnal.ui.activity.CameraActivity
import android.widget.Toast
import com.example.nocturnal.R
import com.example.nocturnal.ui.fragment.LoginFragment

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

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (password == confirmPassword) {
                userViewModel.registerUser(email, password) { isSuccess, errorMessage, uid ->
                    if (isSuccess && uid != null) {
                        userViewModel.storeUsername(uid, username)
                        Toast.makeText(context, "Registration successful", Toast.LENGTH_LONG).show()
                        // Navigate to CameraActivity
                        val intent = Intent(activity, CameraActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(context, "Registration failed: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
            }
        }
//        binding.switchToLogin.setOnClickListener {
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, LoginFragment())
//                .addToBackStack(null)
//                .commit()
//        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}