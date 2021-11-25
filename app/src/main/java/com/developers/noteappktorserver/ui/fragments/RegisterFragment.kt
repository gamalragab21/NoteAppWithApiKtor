package com.developers.noteappktorserver.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.developers.noteappktorserver.R
import com.developers.noteappktorserver.data.local.DataStoreManager
import com.developers.noteappktorserver.databinding.FragmentRegisterBinding
import com.developers.noteappktorserver.entities.Note
import com.developers.noteappktorserver.helpers.EventObserver
import com.developers.noteappktorserver.ui.viewmodels.AuthenticationViewModel
import com.developers.noteappktorserver.utils.Constants
import com.developers.noteappktorserver.utils.NoteUtility
import com.developers.shopapp.utils.snackbar
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class RegisterFragment:Fragment(), EasyPermissions.PermissionCallbacks {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val authenticationViewModel: AuthenticationViewModel by viewModels()

    @Inject
    lateinit var dataStoreManager: DataStoreManager
    private var imageUserUri:String=""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // clear error text
        binding.inputTextUserName.doAfterTextChanged {
            binding.inputTextLayoutUserName.isHelperTextEnabled = false
        }
        binding.inputTextEmail.doAfterTextChanged {
            binding.inputTextLayoutEmail.isHelperTextEnabled = false
        }

        binding.inputTextPassword.doAfterTextChanged {
            binding.inputTextLayoutPassword.isHelperTextEnabled = false
        }

        binding.alreadyHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }
        // sign in
        binding.createAccount.setOnClickListener {
            authenticationViewModel.createAccount(
                binding.inputTextLayoutUserName,
                binding.inputTextLayoutEmail,
                binding.inputTextLayoutPassword,imageUserUri
            )
        }

        binding.userImage.setOnClickListener {
            requestPermissions()
        }

        // check login state
        subscribeToObservables(savedInstanceState)




    }

    private fun subscribeToObservables(savedInstanceState: Bundle?) {
        lifecycleScope.launchWhenStarted {
            authenticationViewModel.createAccountStatus.collect(EventObserver(
                onLoading ={
                    binding.spinKit.isVisible=true
                },
                onSuccess = {
                    binding.spinKit.isVisible=false

                    snackbar(it.message)
                    it.data?.let {
                        lifecycleScope.launch {

                            saveDataAndNavigate(it,savedInstanceState)
                        }
                    }
                },
                onError = {
                    binding.spinKit.isVisible=false
                    snackbar(it)
                }
            ))
        }
    }

    private suspend fun saveDataAndNavigate(token: String, savedInstanceState: Bundle?) {
        updateToken(token)
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.registerFragment, true)
            .build()
        findNavController().navigate(
            R.id.action_registerFragment_to_homeFragment,
            savedInstanceState,
            navOptions
        )
    }

    private fun saveEmailAndPassword(email: String, password: String) {
        lifecycleScope.launch {
            dataStoreManager.setUserInfo(email = email,password = password)
        }
    }

    private suspend fun updateToken(token: String) {
        dataStoreManager.setUserInfo(token = token)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding= FragmentRegisterBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

    private fun requestPermissions() {

        if (NoteUtility.hasReadExternalStoragePermissions(requireContext())) {
            null
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "you need to accept read storage permissions to use this optional.",
                Constants.REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "you need to accept read storage permissions to use this optional.",
                Constants.REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        openGallery()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun openGallery() {
        CropImage.startPickImageActivity(requireContext(), this@RegisterFragment)
    }

    fun cropImage(uri: Uri?) {
        uri?.let { myuri ->
            CropImage.activity(uri)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setMultiTouchEnabled(true)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(requireContext(), this)
        }

    }

    @SuppressLint("CheckResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("here", "onActivityResult: ")
        when (requestCode) {
            CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val uri = CropImage.getPickImageResultUri(requireContext(), data)
                    cropImage(uri)
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    binding.userImage.setImageURI(result.uri)
                    imageUserUri = result.uri.toString()
                }
            }

        }

    }

}