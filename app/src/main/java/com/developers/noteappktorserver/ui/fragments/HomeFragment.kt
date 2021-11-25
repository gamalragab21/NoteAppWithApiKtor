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
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.RequestManager
import com.developers.noteappktorserver.R
import com.developers.noteappktorserver.data.local.DataStoreManager
import com.developers.noteappktorserver.databinding.FragmentHomeBinding
import com.developers.noteappktorserver.entities.Note
import com.developers.noteappktorserver.helpers.EventObserver
import com.developers.noteappktorserver.ui.adapters.NoteAdapter
import com.developers.noteappktorserver.ui.dialogs.AddUrlDialogs
import com.developers.noteappktorserver.ui.viewmodels.HomeViewModel
import com.developers.noteappktorserver.utils.Constants
import com.developers.noteappktorserver.utils.Constants.REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS
import com.developers.noteappktorserver.utils.Constants.SEARCH_TIME_DELAY
import com.developers.noteappktorserver.utils.Constants.TAG
import com.developers.noteappktorserver.utils.NoteUtility
import com.developers.shopapp.utils.snackbar
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var noteAdapter: NoteAdapter
    private val homeViewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var dataStoreManager: DataStoreManager
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // for backPressed
        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callBack)

        binding.addNoteMain.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToCreateNoteFragment(null)
            findNavController().navigate(action)
        }

        var job: Job? = null
        binding.searchNote.addTextChangedListener { editable ->
            job?.cancel()
            job = lifecycleScope.launch {
                delay(SEARCH_TIME_DELAY)
                editable?.let {
                    if (it.isEmpty()) {
                        homeViewModel.getNotes()
                    } else homeViewModel.searchNote(it.toString())
                }
            }
        }

        subscribeToObservers()

        setupRecyclerView()


        noteAdapter.setOnDeleteClickListener { note ->

            note.id?.let {
                homeViewModel.deleteNote(it)
            }
        }

        noteAdapter.setOnNoteClickListener { note ->

            val action = HomeFragmentDirections.actionHomeFragmentToCreateNoteFragment(note)
            findNavController().navigate(action)
        }

        quickActions(savedInstanceState)
//
    }


    @SuppressLint("NewApi")
    private fun quickActions(savedInstanceState: Bundle?) {

        binding.icAddNote.setOnClickListener {

            val action = HomeFragmentDirections.actionHomeFragmentToCreateNoteFragment(null)
            findNavController().navigate(action)
        }

        binding.icAddImage.setOnClickListener {

            addImage()
        }

        binding.icAddUrlWeb.setOnClickListener {

            dialogAddUrl()
        }

        binding.icLogout.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.splashFragment, true)
                .build()
            lifecycleScope.launchWhenStarted {
                dataStoreManager.setUserInfo(token = "")
                findNavController().navigate(
                    R.id.action_homeFragment_to_loginFragment,
                    savedInstanceState,
                    navOptions
                )
            }
        }


    }

    private fun dialogAddUrl() {
        //  findNavController().navigate(R.id.addUrlDialog)
        AddUrlDialogs().apply {
            setPositiveAddUrlListener { url, dialoge ->
                if (url.isNotEmpty()) {
                    val note = Note(
                        subTitle = "",
                        title = "",
                        webLink = url, color = "#FDBE3B",
                        note = "", dataTime = "", id = null, imagePath = null, userId = -1
                    )
                    val action = HomeFragmentDirections.actionHomeFragmentToCreateNoteFragment(note)
                    findNavController().navigate(action)
                } else {
                    snackbar("Url is empty")
                }
                dialoge!!.dismiss()
            }
        }.show(childFragmentManager, null)
    }

    private fun addImage() {
        requestPermissions()
    }

    private fun openGallery() {
        CropImage.startPickImageActivity(requireContext(), this@HomeFragment)
    }

    fun cropImage(uri: Uri?) {
        uri?.let { myuri ->
            CropImage.activity(uri)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
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
                    val imageUri = result.uri
                    //  createNoteViewModel.setCurImageUri( Resource.Success(imageUri))
                    //   createNoteViewModel.setCurImageUri(imageUri)
                    val note = Note(
                       null,
                        "",
                        "", "",
                        imageUri.toString(),
                        "",
                        color = "#FDBE3B",
                        null,
                        userId=-1
                    )


                    val action = HomeFragmentDirections.actionHomeFragmentToCreateNoteFragment(note)
                    findNavController().navigate(action)
                }
            }

        }

    }


    private fun subscribeToObservers() {

        lifecycleScope.launchWhenCreated {

            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    homeViewModel.notesStatus.collect(EventObserver(
                        onLoading = {
                            binding.icProgressNotes.isVisible = true
                            Log.i(Constants.TAG, "onViewCreated:Loding ")
                        },
                        onError = {
                            binding.icProgressNotes.isVisible = false
                            snackbar(it)
                            Log.i(Constants.TAG, "onViewCreated: error $it")
                        },
                        onSuccess = {

                            binding.icProgressNotes.isVisible = false

                            Log.i(Constants.TAG, "onViewCreated: onSuccess $it")

                            // snackbar(it.message)
                            it.data?.let { data ->
                                binding.lnEmpty.isVisible = data.isEmpty()
                                noteAdapter.notes = data
                                Log.i(Constants.TAG, "onViewCreated: data $data")

                            }

                        }
                    ))
                }

                launch {
                    homeViewModel.deleteNoteStatus.collect(EventObserver(
                        onError = {
                            snackbar(it)

                        },
                        onSuccess = {
                            snackbar(it.message)
                            if (it.success) {
                                homeViewModel.getNotes()
                            }
                        },
                        onLoading = {

                        }
                    ))

                }
                launch {
                    homeViewModel.userInfoStatus.collect(EventObserver(
                        onError = {
                            snackbar(it)
                        },
                        onLoading = {

                        },
                        onSuccess = {
                            Log.i(TAG, "subscribeToObservers: ${it.toString()}")
                            if (it.success) {
                                it.data.let {
                                    glide.load(it!!.image).into(binding.userImage)
                                }
                            }
                        }
                    ))
                }
            }
        }
    }

    private fun setupRecyclerView() = binding.rvListNote.apply {
        itemAnimator = null
        isNestedScrollingEnabled = false
        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter = noteAdapter

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun requestPermissions() {

        if (NoteUtility.hasReadExternalStoragePermissions(requireContext())) {
            null
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "you need to accept read storage permissions to use this optional.",
                REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "you need to accept read storage permissions to use this optional.",
                REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSIONS,
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
}