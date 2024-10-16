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
import com.example.nocturnal.ui.fragment.SignUpFragment


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
            val email = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            userViewModel.loginUser(email, password) { isSuccess, errorMessage ->
                if (isSuccess) {
                    // Navigate to CameraActivity
                    val intent = Intent(activity, CameraActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Login failed: $errorMessage", Toast.LENGTH_LONG).show()
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
