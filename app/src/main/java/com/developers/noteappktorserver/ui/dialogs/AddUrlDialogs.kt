package com.developers.noteappktorserver.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.developers.noteappktorserver.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_dialog_add_url.*

@AndroidEntryPoint
class AddUrlDialogs : DialogFragment() {


    private lateinit var dialogView : View

    private var addUrlListener: ((String,Dialog?) -> Unit)? = null

    fun setPositiveAddUrlListener(listener: (String,Dialog?) -> Unit) {
        addUrlListener = listener
    }
    override fun onCreateView(inflater : LayoutInflater , container : ViewGroup? , savedInstanceState : Bundle?) : View {
        return dialogView
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState : Bundle?) : Dialog {
        dialogView = LayoutInflater.from(requireContext()).inflate(
            R.layout.layout_dialog_add_url ,
            null
        )
        return MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setBackground(ColorDrawable(Color.TRANSPARENT))
            .create()
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        btAdd.setOnClickListener {
            addUrlListener?.let { click ->
                click(input_url.text.toString(),dialog)
                dismiss()
            }
        }

        btCancel.setOnClickListener {
            dismiss()
        }


    }

}