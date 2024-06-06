package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.Login.LoginActivity
import com.example.myapplication.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            loadUserProfile(currentUser)
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        return binding.root
    }

    private fun loadUserProfile(currentUser: FirebaseUser) {
        val uid = currentUser.uid
        databaseRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(uid)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userName = dataSnapshot.child("nickName").value.toString()
                val userEmail = currentUser.email

                binding.tvUserEmail.text = "Email: $userEmail"
                binding.tvUserName.text = "Name: $userName"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러 처리
            }
        })
    }

    private fun logout() {
        // FirebaseAuth 로그아웃
        FirebaseAuth.getInstance().signOut()

        // SharedPreferences 로그인 정보 삭제
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("UserId")
        editor.remove("Password")
        editor.remove("AutoLogin")
        editor.apply()

        // 로그인 화면으로 이동
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
