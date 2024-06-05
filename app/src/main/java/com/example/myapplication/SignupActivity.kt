package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySignupBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SignupActivity : AppCompatActivity() {
    lateinit var binding : ActivitySignupBinding
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseRef = FirebaseDatabase.getInstance().getReference()
        val intent = Intent(this, LoginActivity::class.java)

        binding.btnSignup.setOnClickListener {
            var strId = binding.etId.text.toString()
            var strPwd = binding.etPassword.text.toString()

            mFirebaseAuth.createUserWithEmailAndPassword(strId, strPwd).addOnCompleteListener(this, OnCompleteListener {
                if(it.isSuccessful){
                    var firebaseUser: FirebaseUser? = mFirebaseAuth.currentUser
                    var account = UserAccount()
                    account.idToken = firebaseUser!!.uid
                    account.emailId = firebaseUser!!.email.toString()
                    account.password = strPwd

                    mDatabaseRef.child("UserAccount").child(firebaseUser.uid).setValue(account)
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()

                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
