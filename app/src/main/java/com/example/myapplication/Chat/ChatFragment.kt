// ChatFragment.kt
package com.example.myapplication.Chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.databinding.FragmentChatBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {

    lateinit var binding: FragmentChatBinding
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private val chatRoomList = mutableListOf<ChatRoom>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        chatRoomAdapter = ChatRoomAdapter(requireContext(), chatRoomList)
        binding.chatRoomListView.adapter = chatRoomAdapter

        val chatRoomsRef = FirebaseDatabase.getInstance().getReference("ChatRooms")
        chatRoomsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatRoomList.clear()
                for (roomSnapshot in snapshot.children) {
                    val roomId = roomSnapshot.key ?: ""
                    var nickName = ""
                    var lastMessage = ""
                    var lastMessageTime: Long = 0

                    // 토큰 값으로 사용자 닉네임 가져오기
                    val userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(roomId)
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            nickName = userSnapshot.child("nickName").value?.toString() ?: ""
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "Failed to load user info", error.toException())
                        }
                    })

                    // 마지막 메시지 데이터에서 lastMessage, lastMessageTime 가져오기
                    val lastMessageSnapshot = roomSnapshot.children.lastOrNull()
                    if (lastMessageSnapshot != null) {
                        lastMessage = lastMessageSnapshot.child("message").value?.toString() ?: ""
                        lastMessageTime = lastMessageSnapshot.child("timestamp").value as? Long ?: 0
                    }

                    val chatRoom = ChatRoom(
                        roomId,
                        nickName,
                        lastMessage,
                        lastMessageTime
                    )
                    chatRoomList.add(chatRoom)
                }
                chatRoomAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load chat rooms", error.toException())
            }
        })

        chatRoomAdapter.setOnItemClickListener(object : ChatRoomAdapter.OnItemClickListener {
            override fun onItemClick(chatRoom: ChatRoom) {
                val intent = Intent(requireContext(), ChattingActivity::class.java)
                intent.putExtra("token", chatRoom.roomId)
                intent.putExtra("username", chatRoom.lastMessage)
                startActivity(intent)
            }
        })

        return binding.root
    }

    companion object {
        private const val TAG = "ChatFragment"
    }
}

data class ChatRoom(
    val roomId: String,
    val nickName: String, // 채팅방 이름(nickName)
    val lastMessage: String,
    val lastMessageTime: Long
)