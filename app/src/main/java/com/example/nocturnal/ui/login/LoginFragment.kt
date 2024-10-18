package com.example.nocturnal.ui.login

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
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            userViewModel.validateCredentials(username, password) { isValid ->
                if (isValid) {
                    // Navigate to CameraActivity
                    val intent = Intent(activity, CameraActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
