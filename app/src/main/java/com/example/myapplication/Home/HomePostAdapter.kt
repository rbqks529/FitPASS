package com.example.myapplication.Home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemHomePostBinding

class HomePostAdapter(val context: Context, val items : ArrayList<HomePostData>) : RecyclerView.Adapter<HomePostAdapter.ViewHolder>(){

    private lateinit var itemClickerListener: OnItemClickListener

    interface OnItemClickListener{
        fun onImageButtonClick(homePostData: HomePostData)
        /*fun onHeartButtonClick(homePostData: HomePostData)*/
    }

    inner class ViewHolder(val binding: ItemHomePostBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item : HomePostData){
            Glide.with(context)
                .load(item.post_image)
                .into(binding.ivHomePostEdit)

            binding.tvHomePostPlaceName.text = item.post_place_name
            binding.tvHomePostAddr.text = item.post_address
            binding.tvHomePostTime.text = item.post_time.toString()
            binding.tvHomePostPrice.text = item.post_price
            binding.tvHomePostRestTime.text = item.post_rest_time.toString()
            binding.tvHomePostOriginalPrice.text = item.post_original_price
        }
    }

    //setter?
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        itemClickerListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomePostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

}