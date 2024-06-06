package com.example.myapplication.Home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Post.PostDetailFragment
import com.example.myapplication.Post.PostingFragment
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    private var PostitemList: ArrayList<HomePostData> = arrayListOf()
    private var HomePostAdapter: HomePostAdapter? = null
    private lateinit var mDatabaseRef: DatabaseReference
    private lateinit var database: FirebaseDatabase

    private val listener = object : ValueEventListener {
        override fun onDataChange(datasnapshot: DataSnapshot) {
            PostitemList.clear()
            for (snapshot: DataSnapshot in datasnapshot.children.reversed()) { // 내림차순 순회
                val post = snapshot.getValue(HomePostData::class.java)
                post?.let {
                    // 토큰 값 사용
                    val postToken = it.postToken
                    PostitemList.add(it)
                }
            }
            HomePostAdapter?.notifyDataSetChanged()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("HomeFragment", "db에러")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.rvPost.setHasFixedSize(true)

        CoroutineScope(Dispatchers.IO).launch {
            database = FirebaseDatabase.getInstance()
            val query = database.getReference("Post").orderByChild("post_time").limitToLast(50)

            query.addListenerForSingleValueEvent(listener)

            withContext(Dispatchers.Main) {
                initRecyclerView()
            }
        }

        binding.btnGet.setOnClickListener {
            val postFragment = PostingFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, postFragment)
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    private fun initRecyclerView() {
        HomePostAdapter = HomePostAdapter(requireContext(), PostitemList)
        binding.rvPost.adapter = HomePostAdapter
        binding.rvPost.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        HomePostAdapter?.setOnItemClickListener(object : HomePostAdapter.OnItemClickListener {
            override fun onItemClick(homePostData: HomePostData) {
                val postDetailFragment = PostDetailFragment()
                val bundle = Bundle().apply {
                    putSerializable("post", homePostData)
                }
                postDetailFragment.arguments = bundle
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, postDetailFragment).addToBackStack(null)
                    .commit()
            }
        })
    }

}