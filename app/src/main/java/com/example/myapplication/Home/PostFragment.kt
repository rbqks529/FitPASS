package com.example.myapplication.Home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.databinding.FragmentPostBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage


class PostFragment : Fragment() {

    private lateinit var binding: FragmentPostBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    private val REQUEST_IMAGE_PICK = 1
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPostBinding.inflate(inflater, container, false)

        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Post")

        binding.btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnPost.setOnClickListener {
            uploadPost()
        }

        return binding.root
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            binding.ivPostImage.setImageURI(selectedImageUri)
        }
    }

    private fun uploadPost() {
        val placeName = binding.etPostPlaceName.text.toString().trim()
        val address = binding.etPostAddress.text.toString().trim()
        val restTime = binding.etPostRestTime.text.toString().toIntOrNull() ?: 0
        val price = binding.etPostPrice.text.toString().trim()
        val originalPrice = binding.etPostOriginalPrice.text.toString().trim()
        val postTime = System.currentTimeMillis() / 1000 // Get current time in seconds

        if (selectedImageUri != null && placeName.isNotEmpty() && address.isNotEmpty() && price.isNotEmpty() && originalPrice.isNotEmpty()) {
            val postKey = databaseReference.push().key ?: return

            val uploadTask = selectedImageUri?.let { uri ->
                val storageReference = FirebaseStorage.getInstance().reference.child("post_images/$postKey")
                storageReference.putFile(uri)
            }

            uploadTask?.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Failed to upload image")
                }
                task.result?.metadata?.reference?.downloadUrl
            }?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val imageUrl = task.result.toString()
                    val post = HomePostData(imageUrl, placeName, address, postTime.toLong(), restTime, price, originalPrice)
                    databaseReference.child(postKey).setValue(post)
                        .addOnSuccessListener {
                            // Clear input fields and reset the view
                            binding.etPostPlaceName.setText("")
                            binding.etPostAddress.setText("")
                            binding.etPostRestTime.setText("")
                            binding.etPostPrice.setText("")
                            binding.etPostOriginalPrice.setText("")
                            binding.ivPostImage.setImageDrawable(null)
                            selectedImageUri = null
                        }
                        .addOnFailureListener { exception ->
                            // Handle failure
                        }
                } else {
                    // Handle failure
                }
            }
        } else {
            // Show an error message or handle the case where required fields are missing
        }
    }
}
