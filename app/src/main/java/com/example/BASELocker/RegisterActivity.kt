package com.example.BASELocker

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikasi.databinding.ActivityRegisterBinding
import com.google.firebase.database.*
import com.example.aplikasi.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var loading: View
    private var x1: Float = 0f
    private var x2: Float = 0f
    private var y1: Float = 0f
    private var y2: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = binding.loadingPanel

        val database = FirebaseDatabase.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val username = binding.usernameEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()

            if (username.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    loading.visibility = View.VISIBLE // Show the loading animation

                    val userPassRef = database.getReference("user/$username/password")
                    userPassRef.setValue(pass)
                    val userLockerRef = database.getReference("user/$username/locker")
                    userLockerRef.setValue("-")

                    loading.visibility = View.GONE // Hide the loading animation

                    // Registration successful, show a toast or perform other actions
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onTouchEvent(touchEvent: MotionEvent): Boolean {
        when (touchEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = touchEvent.x
                y1 = touchEvent.y
            }
            MotionEvent.ACTION_UP -> {
                x2 = touchEvent.x
                y2 = touchEvent.y
                if (x2> x1) {
                    val i = Intent(this, LoginActivity::class.java)
                    startActivity(i)
                    overridePendingTransition(R.anim.slide_in_left, R.anim.stay)
                }
            }
        }
        return false
    }
}
