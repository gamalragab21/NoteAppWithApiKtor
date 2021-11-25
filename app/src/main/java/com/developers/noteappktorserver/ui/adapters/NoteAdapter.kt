package com.developers.noteappktorserver.ui.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.developers.noteappktorserver.R
import com.developers.noteappktorserver.entities.Note
import kotlinx.android.synthetic.main.item_container_note.view.*
import javax.inject.Inject

class NoteAdapter @Inject constructor(
    private val glide : RequestManager
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {


    var notes : List<Note>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private val diffCallback = object : DiffUtil.ItemCallback<Note>() {
        override fun areContentsTheSame(oldItem : Note , newItem : Note) : Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areItemsTheSame(oldItem : Note , newItem : Note) : Boolean {
            return oldItem.id == newItem.id
        }
    }
    private val differ = AsyncListDiffer(this , diffCallback)

    class NoteViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val imageNote = itemView.image_list_note
        val titleNote = itemView.titleNote
        val subtitleNote = itemView.subtitleNote
        val textDataTimeNote = itemView.textDataTimeNote
        val urlItem = itemView.urlItem
        val ic_delete_note_list = itemView.ic_delete_note_list
        val cardNote = itemView.cardNote
    }

    override fun onCreateViewHolder(parent : ViewGroup , viewType : Int) : NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_container_note ,
                parent ,
                false
            )
        )
    }

    @SuppressLint("Range")
    override fun onBindViewHolder(holder : NoteViewHolder , position : Int) {
        val note = notes[position]
        holder.apply {
            note.imagePath?.let {uri->
                if (uri.isNotEmpty())
                glide.load(uri).into(imageNote)
            }
            titleNote.text = note.title
            subtitleNote.text = note.subTitle
            textDataTimeNote.text = note.dataTime


                note.webLink?.let {uri->
                    urlItem.text=uri
                }


            itemView.setOnClickListener {
                onNoteClickListener?.let { click ->
                    click(note)
                }
            }
            ic_delete_note_list.setOnClickListener {
                onDeleteClickListener?.let { click ->
                    click(note)
                }
            }


            note.color.let { color ->
                if (color.endsWith("#FFFFFF")) {
                    titleNote.setTextColor(Color.parseColor("#000000"))
                    subtitleNote.setTextColor(Color.parseColor("#000000"))
                    textDataTimeNote.setTextColor(Color.parseColor("#000000"))
                } else {
                    titleNote.setTextColor(Color.parseColor("#FFFFFF"))
                    subtitleNote.setTextColor(Color.parseColor("#FFFFFF"))
                    textDataTimeNote.setTextColor(Color.parseColor("#FFFFFF"))
                }
              //  try {
                    cardNote.setCardBackgroundColor(Color.parseColor(color))
//                } catch (e : Exception) {
//                    cardNote.setCardBackgroundColor(Color.parseColor("#333333"))
//                }

            }

        }
    }

    override fun getItemCount() : Int = notes.size


    // click options
    private var onNoteClickListener : ((Note) -> Unit)? = null

    fun setOnNoteClickListener(listener : (Note) -> Unit) {
        onNoteClickListener = listener
    }

    private var onDeleteClickListener : ((Note) -> Unit)? = null

    fun setOnDeleteClickListener(listener : (Note) -> Unit) {
        onDeleteClickListener = listener
    }

}