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
    private lateinit var token: String
    private lateinit var username: String
    private lateinit var chatRoomRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        token = intent.getStringExtra("token") ?: ""
        username = intent.getStringExtra("username") ?: ""

        chatRoomRef = FirebaseDatabase.getInstance().getReference("ChatRooms").child(token)

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
                    binding.messageListView.adapter = messageAdapter

                    addUserToChatRoom(uid)
                    setupMessageListener()
                    loadChatHistory()

                    try {
                        socket = IO.socket("http://218.38.190.81:10001")
                        socket.connect()
                        socket.emit("joinRoom", JSONObject().put("roomId", token).put("username", username))
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

                                val chatMessage = ChatMessage(message, sender, System.currentTimeMillis())
                                chatRoomRef.child("messages").push().setValue(chatMessage)

                                // 마지막 메시지 및 시간 업데이트
                                chatRoomRef.child("lastMessage").setValue(message)
                                chatRoomRef.child("lastMessageTime").setValue(System.currentTimeMillis())
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

    private fun addUserToChatRoom(uid: String) {
        chatRoomRef.child("participants").child(uid).setValue(true).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "User added to chat room")
            } else {
                Log.e(TAG, "Failed to add user to chat room")
            }
        }
    }

    private fun setupMessageListener() {
        chatRoomRef.child("messages").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.child("message").value.toString()
                    val sender = messageSnapshot.child("sender").value.toString()
                    messageList.add(Message(message, sender))
                }
                messageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load chat messages", error.toException())
            }
        })
    }

    private fun loadChatHistory() {
        chatRoomRef.child("messages").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.child("message").value.toString()
                    val sender = messageSnapshot.child("sender").value.toString()
                    messageList.add(Message(message, sender))
                }
                messageAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load chat history", error.toException())
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        socket.disconnect()
    }

    companion object {
        private const val TAG = "ChatActivity"
    }
}

data class ChatMessage(
    val message: String,
    val sender: String,
    val timestamp: Long
)

// MessageAdapter.kt 파일은 수정 필요 없음
