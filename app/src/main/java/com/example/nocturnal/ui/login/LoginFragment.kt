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
            val newUser = mapOf(
                "username" to usernameEditText.text.toString(),
                "password" to passwordEditText.text.toString()
            )
            userViewModel.addUser(newUser)

            // Navigate to CameraActivity
            val intent = Intent(activity, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
