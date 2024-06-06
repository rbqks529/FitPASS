package com.example.myapplication.Chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.myapplication.R

class ChatRoomAdapter(private val context: Context, private val chatRooms: List<ChatRoom>) :
    ArrayAdapter<ChatRoom>(context, 0, chatRooms) {

    private lateinit var itemClickListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(chatRoom: ChatRoom)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val chatRoom = getItem(position)
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_chat_room, parent, false)
        }

        val roomNameTextView: TextView = view!!.findViewById(R.id.roomNameTextView)

        roomNameTextView.text = chatRoom!!.postName

        view.setOnClickListener {
            chatRoom.let { itemClickListener.onItemClick(it) }
        }

        return view
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }
}