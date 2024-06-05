package com.example.myapplication.Chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {

    lateinit var binding: FragmentChatBinding
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private val chatRoomList = mutableListOf<ChatRoom>()

    private lateinit var chatRoomsRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser?.uid ?: ""

        chatRoomAdapter = ChatRoomAdapter(requireContext(), chatRoomList)
        binding.chatRoomListView.adapter = chatRoomAdapter

        chatRoomsRef = FirebaseDatabase.getInstance().getReference("ChatRooms")

        // 사용자가 참여한 채팅방을 가져오기
        fetchUserChatRooms()

        chatRoomAdapter.setOnItemClickListener(object : ChatRoomAdapter.OnItemClickListener {
            override fun onItemClick(chatRoom: ChatRoom) {
                val intent = Intent(requireContext(), ChattingActivity::class.java)
                intent.putExtra("token", chatRoom.roomId)
                intent.putExtra("username", chatRoom.nickName)
                startActivity(intent)
            }
        })

        return binding.root
    }

    private fun fetchUserChatRooms() {
        chatRoomsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatRoomList.clear()
                for (roomSnapshot in snapshot.children) {
                    val participantsSnapshot = roomSnapshot.child("participants")
                    if (participantsSnapshot.hasChild(currentUserID)) {
                        val roomId = roomSnapshot.key ?: ""
                        val lastMessage = roomSnapshot.child("lastMessage").getValue(String::class.java) ?: ""
                        val lastMessageTime = roomSnapshot.child("lastMessageTime").getValue(Long::class.java) ?: 0

                        val chatRoom = ChatRoom(
                            roomId,
                            "", // 초기에는 빈 닉네임을 설정
                            lastMessage,
                            lastMessageTime
                        )

                        // 닉네임을 가져와서 설정하기
                        val userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(currentUserID)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val nickName = userSnapshot.child("nickName").getValue(String::class.java) ?: ""
                                chatRoom.nickName = nickName
                                chatRoomList.add(chatRoom)
                                chatRoomAdapter.notifyDataSetChanged()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(TAG, "Failed to load user info", error.toException())
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load user's chat rooms", error.toException())
            }
        })
    }

    companion object {
        private const val TAG = "ChatFragment"
    }
}

data class ChatRoom(
    val roomId: String,
    var nickName: String, // 채팅방 이름(nickName)
    val lastMessage: String,
    val lastMessageTime: Long
)
