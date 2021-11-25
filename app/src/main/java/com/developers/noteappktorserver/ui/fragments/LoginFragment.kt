package com.developers.noteappktorserver.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.developers.noteappktorserver.R
import com.developers.noteappktorserver.data.local.DataStoreManager
import com.developers.noteappktorserver.databinding.FragmentLoginBinding
import com.developers.noteappktorserver.helpers.EventObserver
import com.developers.noteappktorserver.ui.viewmodels.AuthenticationViewModel
import com.developers.noteappktorserver.utils.Constants
import com.developers.shopapp.utils.snackbar

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment:Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!


    private val authenticationViewModel: AuthenticationViewModel by viewModels()

    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // to get email , password if sets it as remember me
        val dataUserInfo = dataStoreManager.glucoseFlow.value
        binding.inputTextEmail.setText(dataUserInfo?.email)
        binding.inputTextPassword.setText(dataUserInfo?.password)
        Log.i(Constants.TAG, "dataUserInfo: ${dataUserInfo.toString()}")
        // clear error text
        binding.inputTextEmail.doAfterTextChanged {
            binding.inputTextLayoutEmail.isHelperTextEnabled = false
        }

        binding.inputTextPassword.doAfterTextChanged {
            binding.inputTextLayoutPassword.isHelperTextEnabled = false
        }

        binding.textSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.forgetPassword.setOnClickListener {
            //findNavController().navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
        }
        binding.backIcon.setOnClickListener {
            requireActivity().finish()
        }
        // sign in
        binding.singinBtn.setOnClickListener {

            authenticationViewModel.loginUser(
                binding.inputTextLayoutEmail,
                binding.inputTextLayoutPassword,
            )
        }

        // check login state
        subscribeToObservables(savedInstanceState)




    }

    private fun subscribeToObservables(savedInstanceState: Bundle?) {
        lifecycleScope.launchWhenCreated {
            authenticationViewModel.authLoginUserStatus.collect(EventObserver(
                onLoading = {
                    binding.spinKit.isVisible=true

                    Log.i(Constants.TAG, "onViewCreated: Loding ")

                },
                onError = {
                    snackbar(it)
                    binding.spinKit.isVisible=false
                    Log.i(Constants.TAG, "onViewCreated: ERROR $it")

                },
                onSuccess = {
                    binding.spinKit.isVisible=false
                    Log.i(Constants.TAG, "onViewCreated: onSuccess $it")
                    snackbar(it.message)
                    it.data?.let { token ->
                        lifecycleScope.launch {
                            saveDataAndNavigate(token,savedInstanceState)
                        }
                    }

                }
            ))
        }

    }
    private suspend fun saveDataAndNavigate(token: String, savedInstanceState: Bundle?) {
        updateToken(token)
        if (binding.switchRememberMe.isChecked) {
            val password = binding.inputTextPassword.text.toString()
            val email = binding.inputTextEmail.text.toString()
            saveEmailAndPassword(email, password)
        }

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.loginFragment, true)
            .build()
        findNavController().navigate(
            R.id.action_loginFragment_to_homeFragment,
            savedInstanceState,
            navOptions
        )
    }

    private suspend fun saveEmailAndPassword(email: String, password: String) {
            dataStoreManager.setUserInfo(email = email, password = password)

    }

    private suspend fun updateToken(token: String) {
        dataStoreManager.setUserInfo(token = token)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding= FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}