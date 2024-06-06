package com.example.myapplication.Login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding

    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mDatabaseRef: DatabaseReference
    private val PREFS_NAME = "MyPrefs"
    private val PREF_USER_ID = "UserId"
    private val PREF_PASSWORD = "Password"
    private val PREF_AUTO_LOGIN = "AutoLogin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFirebaseAuth = FirebaseAuth.getInstance()
        mDatabaseRef = FirebaseDatabase.getInstance().getReference()

        // Check for saved login info
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = sharedPreferences.getString(PREF_USER_ID, null)
        val savedPassword = sharedPreferences.getString(PREF_PASSWORD, null)
        val isAutoLogin = sharedPreferences.getBoolean(PREF_AUTO_LOGIN, false)

        if (isAutoLogin && !savedUserId.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            // Attempt auto login
            mFirebaseAuth.signInWithEmailAndPassword(savedUserId, savedPassword).addOnCompleteListener(this, OnCompleteListener {
                if(it.isSuccessful) {
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "자동 로그인 실패", Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.btnSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val userId = binding.etId.text.toString()
            val password = binding.etPassword.text.toString()
            val autoLogin = binding.cbAutoLogin.isChecked

            mFirebaseAuth.signInWithEmailAndPassword(userId, password).addOnCompleteListener(this, OnCompleteListener {
                if(it.isSuccessful) {
                    if (autoLogin) {
                        saveLoginInfo(userId, password, autoLogin)
                    } else {
                        clearLoginInfo()
                    }
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun saveLoginInfo(userId: String, password: String, autoLogin: Boolean) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(PREF_USER_ID, userId)
        editor.putString(PREF_PASSWORD, password)
        editor.putBoolean(PREF_AUTO_LOGIN, autoLogin)
        editor.apply()
    }

    private fun clearLoginInfo() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(PREF_USER_ID)
        editor.remove(PREF_PASSWORD)
        editor.remove(PREF_AUTO_LOGIN)
        editor.apply()
    }
}
