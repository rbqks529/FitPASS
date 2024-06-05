package com.example.myapplication.Home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentHomeBinding



class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    private var PostitemList : ArrayList<HomePostData> = arrayListOf()
    private var HomePostAdapter : HomePostAdapter ?= null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)




        return binding.root
    }

    private fun initRecyclerView() {
        HomePostAdapter = HomePostAdapter(requireContext(), PostitemList)
        binding.rvPost.adapter = HomePostAdapter
        binding.rvPost.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
    }

}