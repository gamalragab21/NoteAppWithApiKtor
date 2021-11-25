package com.developers.noteappktorserver.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.developers.noteappktorserver.R
import com.developers.noteappktorserver.databinding.FragmentCreateNoteBinding
import com.developers.noteappktorserver.databinding.FragmentHomeBinding
import com.developers.noteappktorserver.databinding.FragmentLoginBinding
import com.developers.noteappktorserver.entities.Note
import com.developers.noteappktorserver.helpers.EventObserver
import com.developers.noteappktorserver.ui.dialogs.AddUrlDialogs
import com.developers.noteappktorserver.ui.viewmodels.CreateNoteViewModel
import com.developers.noteappktorserver.utils.Constants
import com.developers.noteappktorserver.utils.NoteUtility
import com.developers.shopapp.utils.hideKeyboard
import com.developers.shopapp.utils.snackbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_create_note.*
import kotlinx.android.synthetic.main.item_container_note.*
import kotlinx.android.synthetic.main.layout_dialog_add_url.*
import kotlinx.android.synthetic.main.layout_persistent_bottom_sheet.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class CreateNoteFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentCreateNoteBinding? = null
    private val binding get() = _binding!!

    private val TAG = "CreateNoteFragment"
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var selecteColorNote: String = "#FDBE3B"

    private val createNoteViewModel: CreateNoteViewModel by viewModels()

    @Inject
    lateinit var glide: RequestManager


    private val args: CreateNoteFragmentArgs by navArgs()
//    private var note:Note?=null

    private var isUpdate = false

    private var idOfNoteUpdated: Int? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        args.note?.let { noteUpdate ->
            noteUpdate.id?.let {
                isUpdate = true
                idOfNoteUpdated = noteUpdate.id
            }



            binding.inputTitle.setText(noteUpdate.title)
            binding.textDataTime.text = noteUpdate.dataTime
            binding.inputSubTitle.setText(noteUpdate.subTitle)
            binding.inputNote.setText(noteUpdate.note)
            noteUpdate.imagePath?.let {
                if (it.isNotEmpty()) {
                    val uri = Uri.parse(it)
                    glide.load(uri).into(binding.imageNote)
                }
            }
            noteUpdate.webLink?.let {
                if (it.isNotEmpty()) binding.textUrl.text = it
            }

            setNoteColorIndicator()
            noteUpdate.webLink?.let { url ->
                createNoteViewModel.setWebLink(url, null)
            }
            noteUpdate.imagePath?.let { uri ->
                createNoteViewModel.setCurImageUri(Uri.parse(uri))
            }

            noteUpdate.color?.let { color ->
                selecteColorNote = color
            }
        }

        // foundations
        subscribeToObservers()
        initBottomSheet()
        setNoteColorIndicator()

        createNoteViewModel.setColorIndicatorStatus(selecteColorNote)


        // clicks
        binding.icBack.setOnClickListener {
            it.hideKeyboard()
            findNavController().popBackStack()
        }

        binding.icSave.setOnClickListener {
            it.hideKeyboard()
            if (!isUpdate) {
                createNoteViewModel.saveNote(
                    binding.inputTitle,
                    binding.inputSubTitle,
                    binding.inputNote
                )
            } else {
                idOfNoteUpdated?.let {
                    createNoteViewModel.updateNote(it, input_title, input_subTitle, input_note)
                }
            }

        }
    }

    private fun subscribeToObservers() {


        lifecycleScope.launchWhenCreated {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // for time and data
                createNoteViewModel.timeStatus.observe(viewLifecycleOwner, {
                    if (!isUpdate) binding.textDataTime.text = it
                })
                // for image uri
                createNoteViewModel.curImageUri.observe(viewLifecycleOwner, {
                    //  glide.load(it).into(binding.imageNote)
                    Glide.with(requireContext()).load(it).into(binding.imageNote)
                })
                // for color indicator
                createNoteViewModel.colorIndicatorStatus.observe(
                    viewLifecycleOwner, {
                        it?.let {
                            setColorIndicator(it)
                        }
                    }
                )

                // for save note
                launch {
                    createNoteViewModel.createNoteStatus.collect(EventObserver(
                        onError = {
                            snackbar(it)
                            binding.icProgressCreateNote.isVisible = false
                        },
                        onLoading = {
                            binding.icProgressCreateNote.isVisible = true

                        }
                    ) { response ->
                        binding.icProgressCreateNote.isVisible = false
                        if (response.success) {
                            snackbar(getString(R.string.save_note))
                            findNavController().navigate(CreateNoteFragmentDirections.actionCreateNoteFragmentToHomeFragment())
                        }
                    })
                }


                // for update note
                launch {
                    createNoteViewModel.updateNoteStatus.collect(EventObserver(
                        onError = {
                            snackbar(it)
                            Log.i(TAG, "subscribeToObservers: $it")
                            binding.icProgressCreateNote.isVisible = false
                        },
                        onLoading = {
                            binding.icProgressCreateNote.isVisible = true

                        }
                    ) { data ->
                        binding.icProgressCreateNote.isVisible = false
                        snackbar(data.message)

                        if (data.success) {

                            findNavController().navigate(CreateNoteFragmentDirections.actionCreateNoteFragmentToHomeFragment())

                        }
                    })
                }

                launch {
                    createNoteViewModel.deleteNoteStatus.collect(EventObserver(
                        onError = {
                            snackbar(it)
                        },
                        onSuccess = {
                            snackbar(it.message)
                            if (it.success) {
                                findNavController().navigate(CreateNoteFragmentDirections.actionCreateNoteFragmentToHomeFragment())
                            }
                        },
                        onLoading = {

                        }
                    ))

                }
                // for add url
                launch {
                    createNoteViewModel.webLinkStatus.collect(EventObserver(
                        onError = {
                            snackbar(it)
                            binding.icProgressCreateNote.isVisible = false
                        },
                        onLoading = {
                            binding.icProgressCreateNote.isVisible = true
                        }
                    ) { url ->
                        binding.icProgressCreateNote.isVisible = false
                        textUrl.apply {
                            delete_uri.isVisible = url.isNotEmpty()
                            text = url
                        }
                    })
                    // set uri image
                    createNoteViewModel.curImageUri.observe(viewLifecycleOwner, {
                        glide.load(it).into(binding.imageNote)
                    })

                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun initBottomSheet() {

        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)

        Miscellaneous.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    delete_note.isVisible = idOfNoteUpdated != null

                    setNoteColorIndicator()
                    bottomSheet.hideKeyboard()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })

        ic_add_image_bottom_sheet.setOnClickListener {
            addImage()
        }
        ic_add_url_bottom_sheet.setOnClickListener {
            dialogAddUrl()
        }

        ic_copy.setOnClickListener {
            copyTextNote()
        }

        delete_note.setOnClickListener {
            dialogDeleteNote()
        }

        delete_uri.setOnClickListener {
            createNoteViewModel.clearUri()
        }
    }

    private fun addImage() {

        requestPermissions()
    }

    private fun openGallery() {
        CropImage.startPickImageActivity(requireContext(), this@CreateNoteFragment)
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
                    createNoteViewModel.setCurImageUri(imageUri)
                }
            }

        }

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

    private fun dialogDeleteNote() {
        idOfNoteUpdated?.let {
            createNoteViewModel.deleteNote(it)
        }
    }

    private fun copyTextNote() {
        val clipBoard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Text Note", binding.inputNote.text.toString())
        if (clipBoard != null && clipData != null && binding.inputNote.text.toString().trim()
                .isNotEmpty()
        ) {
            clipBoard.setPrimaryClip(clipData)
            snackbar(getString(R.string.text_copied))
        }
    }

    private fun dialogAddUrl() {
        //  findNavController().navigate(R.id.addUrlDialog)
        AddUrlDialogs().apply {
            setPositiveAddUrlListener { url, dialoge ->
                if (url.isNotEmpty()) createNoteViewModel.setWebLink(url, dialoge)
                else snackbar("Url must not empty")
            }
        }.show(childFragmentManager, null)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

    }

    private fun setNoteColorIndicator() {
        noteDefault1.setOnClickListener {
            ic_done_note1.visibility = View.VISIBLE
            ic_done_note2.visibility = View.GONE
            ic_done_note3.visibility = View.GONE
            ic_done_note4.visibility = View.GONE
            ic_done_note5.visibility = View.GONE
            selecteColorNote = "#333333"
            createNoteViewModel.setColorIndicatorStatus(selecteColorNote)
        }
        note2.setOnClickListener {
            ic_done_note2.visibility = View.VISIBLE
            ic_done_note1.visibility = View.GONE
            ic_done_note3.visibility = View.GONE
            ic_done_note4.visibility = View.GONE
            ic_done_note5.visibility = View.GONE
            selecteColorNote = "#FDBE3B"
            createNoteViewModel.setColorIndicatorStatus(selecteColorNote)

        }
        note3.setOnClickListener {
            ic_done_note3.visibility = View.VISIBLE
            ic_done_note1.visibility = View.GONE
            ic_done_note2.visibility = View.GONE
            ic_done_note4.visibility = View.GONE
            ic_done_note5.visibility = View.GONE
            selecteColorNote = "#2196F3"
            createNoteViewModel.setColorIndicatorStatus(selecteColorNote)
        }
        note4.setOnClickListener {
            ic_done_note4.visibility = View.VISIBLE
            ic_done_note1.visibility = View.GONE
            ic_done_note3.visibility = View.GONE
            ic_done_note2.visibility = View.GONE
            ic_done_note5.visibility = View.GONE
            selecteColorNote = "#3A52Fc"
            createNoteViewModel.setColorIndicatorStatus(selecteColorNote)

        }
        note5.setOnClickListener {
            ic_done_note5.visibility = View.VISIBLE
            ic_done_note1.visibility = View.GONE
            ic_done_note3.visibility = View.GONE
            ic_done_note4.visibility = View.GONE
            ic_done_note2.visibility = View.GONE
            selecteColorNote = "#FFFFFF"
            createNoteViewModel.setColorIndicatorStatus(selecteColorNote)

        }

        when (selecteColorNote) {
            "#333333" -> {
                ic_done_note1.visibility = View.VISIBLE
                ic_done_note2.visibility = View.GONE
                ic_done_note3.visibility = View.GONE
                ic_done_note4.visibility = View.GONE
                ic_done_note5.visibility = View.GONE
            }
            "#FDBE3B" -> {
                ic_done_note2.visibility = View.VISIBLE
                ic_done_note1.visibility = View.GONE
                ic_done_note3.visibility = View.GONE
                ic_done_note4.visibility = View.GONE
                ic_done_note5.visibility = View.GONE
            }
            "#2196F3" -> {
                ic_done_note3.visibility = View.VISIBLE
                ic_done_note1.visibility = View.GONE
                ic_done_note2.visibility = View.GONE
                ic_done_note4.visibility = View.GONE
                ic_done_note5.visibility = View.GONE
            }
            "#3A52Fc" -> {
                ic_done_note4.visibility = View.VISIBLE
                ic_done_note1.visibility = View.GONE
                ic_done_note3.visibility = View.GONE
                ic_done_note2.visibility = View.GONE
                ic_done_note5.visibility = View.GONE
            }
            "#FFFFFF" -> {
                ic_done_note5.visibility = View.VISIBLE
                ic_done_note1.visibility = View.GONE
                ic_done_note3.visibility = View.GONE
                ic_done_note4.visibility = View.GONE
                ic_done_note2.visibility = View.GONE
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun setColorIndicator(selecteColorNote: String) {
        val drawable = view_indicator.background as Drawable
        val color = Color.parseColor(selecteColorNote)
        drawable.setColorFilter(color, PorterDuff.Mode.SRC)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateNoteBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}