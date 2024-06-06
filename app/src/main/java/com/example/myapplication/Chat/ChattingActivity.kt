// ChattingActivity.kt
package com.example.myapplication.Chat

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityChattingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

data class Message(val content: String, val sender: String)

class ChattingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChattingBinding
    private lateinit var socket: Socket
    private val messageList = mutableListOf<Message>()
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var Posttoken: String
    private lateinit var Usertoken: String
    private lateinit var username: String
    private lateinit var postname: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Posttoken = intent.getStringExtra("posttoken") ?: ""
        /*Usertoken = intent.getStringExtra("usertoken") ?: ""*/
        postname = intent.getStringExtra("postname") ?: ""

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            val database = FirebaseDatabase.getInstance().getReference("UserAccount").child(uid)

            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    username = dataSnapshot.child("nickName").value.toString()
                    Log.d(TAG, "username: $username")

                    messageAdapter = MessageAdapter(this@ChattingActivity, messageList, username)
                    binding.messageRecyclerView.adapter = messageAdapter

                    try {
                        socket = IO.socket("http://218.38.190.81:10001")
                        socket.connect()
                        socket.emit("joinRoom", JSONObject().put("roomId", Posttoken).put("username", username))
                    } catch (e: URISyntaxException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    socket.off("message")
                    socket.off("chatHistory")

                    socket.on("chatHistory") { args ->
                        runOnUiThread {
                            val data = args[0] as JSONArray
                            try {
                                for (i in 0 until data.length()) {
                                    val messageData = data.getJSONObject(i)
                                    val message = messageData.getString("message")
                                    val sender = messageData.getString("username")
                                    messageList.add(Message(message, sender))
                                }
                                messageAdapter.notifyDataSetChanged()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }

                    socket.on("message") { args ->
                        runOnUiThread {
                            val data = args[0] as JSONObject
                            try {
                                val message = data.getString("message")
                                val sender = data.getString("username")
                                messageList.add(Message(message, sender))
                                messageAdapter.notifyDataSetChanged()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "loadUser:onCancelled", databaseError.toException())
                }
            })
        } else {
            Log.d(TAG, "No user is logged in")
        }

        binding.ivChatPrevious.setOnClickListener {
            finish()
        }

        binding.sendButton.setOnClickListener {
            val message = binding.messageEditText.text.toString()
            if (message.isNotEmpty()) {
                try {
                    val messageObject = JSONObject()
                    messageObject.put("message", message)
                    messageObject.put("username", username)
                    socket.emit("message", messageObject)

                    binding.messageEditText.setText("")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    companion object {
        private const val TAG = "ChatActivity"
    }
}