package com.example.myapplication.Post

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.myapplication.Chat.ChattingActivity
import com.example.myapplication.Home.HomePostData
import com.example.myapplication.databinding.FragmentPostDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class PostDetailFragment : Fragment() {

    private lateinit var binding: FragmentPostDetailBinding
    private lateinit var post: HomePostData
    private lateinit var database: FirebaseDatabase
    private lateinit var userReference: DatabaseReference
    private var token: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false)

        post = arguments?.getSerializable("post") as HomePostData
        token = post.userId

        database = FirebaseDatabase.getInstance()
        userReference = database.reference.child("UserAccount")

        binding.ivPrevious.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.ivHome.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        loadPostDetails()
        loadUserDetails()

        binding.ivChattingButton.setOnClickListener {
            val intent = Intent(requireContext(), ChattingActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("USERNAME", binding.tvPostDetailUsername.text.toString())
            startActivity(intent)
        }

        return binding.root
    }

    private fun loadPostDetails() {
        Glide.with(requireContext())
            .load(post.post_image)
            .into(binding.ivPostDetailImage)

        binding.tvPostDetailPlaceName.text = post.post_place_name
        binding.tvPostDetailAddress.text = post.post_address
        binding.tvPostDetailPrice.text = post.post_price
        binding.tvPostDetailTime.text = getTimeDifferenceString(post.post_time)
        binding.tvPostDetailContent.text = post.post_content // 게시글 내용 표시
    }

    private fun loadUserDetails() {
        token?.let { token ->
            userReference.child(token).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(object : GenericTypeIndicator<Map<String, Any>>() {})
                    userData?.let {
                        binding.tvPostDetailUsername.text = it["nickName"].toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun getTimeDifferenceString(postTime: Long): String {
        val currentTime = System.currentTimeMillis() / 1000 // 현재 시간을 초 단위로 변환
        val timeDiff = currentTime - postTime
        val seconds = timeDiff
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7

        return when {
            days == 0L && hours == 0L && minutes == 0L -> "몇 초 전"
            days == 0L && hours == 0L -> "${minutes}분 전"
            days == 0L -> "${hours}시간 전"
            days < 7 -> "${days}일 전"
            else -> "${weeks}주 전"
        }
    }
}
