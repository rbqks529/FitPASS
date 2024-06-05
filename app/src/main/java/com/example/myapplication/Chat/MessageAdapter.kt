// MessageAdapter.kt
package com.example.myapplication.Chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.myapplication.R

class MessageAdapter(
    private val context: Context, private val messages: List<Message>, private val currentUserName: String)
    : ArrayAdapter<Message>(context, 0, messages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val message = getItem(position)
        var view = convertView
        val viewType = getItemViewType(position)

        if (view == null) {
            view = when (viewType) {
                0 -> LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false)
                else -> LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false)
            }
        }

        val messageTextView: TextView = view!!.findViewById(R.id.messageTextView)
        messageTextView.text = message?.content

        return view
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message?.sender == currentUserName) 0 else 1
    }

    override fun getViewTypeCount(): Int {
        return 2
    }
}