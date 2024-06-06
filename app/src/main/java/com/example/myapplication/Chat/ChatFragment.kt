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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private val chatRoomList = mutableListOf<ChatRoom>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid ?: ""


        chatRoomAdapter = ChatRoomAdapter(requireContext(), chatRoomList)
        binding.chatRoomListView.adapter = chatRoomAdapter

        val chatRoomsRef = FirebaseDatabase.getInstance().getReference("ChatRooms").child(currentUserId)
        chatRoomsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatRoomList.clear()
                for (roomSnapshot in snapshot.children) {
                    val roomId = roomSnapshot.key ?: ""
                    val postName = roomSnapshot.child("postName").value?.toString() ?: ""
                    val placeName = roomSnapshot.child("placeName").value?.toString() ?: ""
                    val price = roomSnapshot.child("price").value?.toString() ?: ""
                    val chatRoom = ChatRoom(roomId, postName, placeName, price)
                    chatRoomList.add(chatRoom)
                }
                chatRoomAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to load chat rooms", error.toException())
            }
        })

        // ChatFragment
        chatRoomAdapter.setOnItemClickListener(object : ChatRoomAdapter.OnItemClickListener {
            override fun onItemClick(chatRoom: ChatRoom) {
                val intent = Intent(requireContext(), ChattingActivity::class.java)
                intent.putExtra("posttoken", chatRoom.roomId)
                intent.putExtra("postname", chatRoom.postName)
                intent.putExtra("placename", chatRoom.placeName)
                intent.putExtra("price", chatRoom.price)
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
    val postName: String?,
    val placeName: String?,
    val price: String?
)