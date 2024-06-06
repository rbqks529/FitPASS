package com.example.myapplication.Home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.ItemHomePostBinding

class HomePostAdapter(val context: Context, val items : ArrayList<HomePostData>) : RecyclerView.Adapter<HomePostAdapter.ViewHolder>(){

    private lateinit var itemClickListener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClick(homePostData: HomePostData)
    }

    inner class ViewHolder(val binding: ItemHomePostBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item : HomePostData){
            Glide.with(context)
                .load(item.post_image)
                .into(binding.ivHomePostEdit)

            binding.tvHomePostPlaceName.text = item.post_place_name
            binding.tvHomePostAddr.text = item.post_address
            binding.tvHomePostTime.text = getTimeDifferenceString(item.post_time)
            binding.tvHomePostPrice.text = item.post_price
            binding.tvHomePostRestTime.text = item.post_rest_time.toString()
            binding.tvHomePostOriginalPrice.text = item.post_original_price

            binding.root.setOnClickListener {
                itemClickListener.onItemClick(item)
            }
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomePostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    private fun getTimeDifferenceString(postTime: Long): String {
        val currentTime = System.currentTimeMillis() / 1000 // 현재 시간을 초 단위로 변환
        val timeDiff = currentTime - postTime
        val seconds = timeDiff
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = days / 30 // 달 단위 계산 추가

        return when {
            days == 0L && hours == 0L && minutes == 0L -> "몇 초 전"
            days == 0L && hours == 0L -> "${minutes}분 전"
            days == 0L -> "${hours}시간 전"
            days < 7 -> "${days}일 전"
            days < 30 -> "${weeks}주 전" // 한 달 미만일 때는 주 단위로 표시
            else -> "${months}달 전" // 한 달 이상일 때는 달 단위로 표시
        }
    }

}