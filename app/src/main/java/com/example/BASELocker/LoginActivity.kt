package com.example.BASELocker


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.example.aplikasi.databinding.ActivityLoginBinding

fun getUsernameandPassValue(lockerRef: DatabaseReference, onStatusValue: (String, String?) -> Unit) {
    lockerRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val value = dataSnapshot. getValue()?.toString()
            if (value != null) {
                Log.i("string value", value)
            }
//                val gson = Gson()
//                val locker = gson.fromJson(statusValue, locker::class.java)
            val pass = value?.substringAfter("password=")?.substringBefore(",").toString()
            val locker = value?.substringAfter("locker=")?.substringBefore("}").toString()
            onStatusValue(pass,locker)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.d("fail", "Failed to read value.", error.toException())

        }
    })
}

private fun checkCredentials(
    username: String,
    password: String,
    onCredentialsChecked: (String, String?) -> Unit,
    onLoadingStarted: () -> Unit,
    onLoadingFinished: () -> Unit
) {
    onLoadingStarted() // Show the progress bar

    val database = FirebaseDatabase.getInstance()
    val userRef = database.getReference("user/$username")
    var pwd: String? = null
    var lockerString: String? = null
    getUsernameandPassValue(userRef) { pass, locker ->
        pwd = pass
        lockerString = locker
        onCredentialsChecked(pwd!!, lockerString)

        onLoadingFinished() // Hide the progress bar
    }
}



class   LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loading = binding.loadingPanel

        binding.textView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val username = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
            } else {
                loading.visibility = View.VISIBLE // Show the progress bar

                checkCredentials(username, password, { pwd, lockerString ->
                    if (password == pwd) {

                        // Start the MainActivity and pass the username as an extra
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("username", username)
                        intent.putExtra("password", pwd)
                        intent.putExtra("locker", lockerString)

                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                    }
                }, {
                    // onLoadingStarted: Show the progress bar (already done above)
                }, {
                    loading.visibility = View.GONE // Hide the progress bar
                })
            }


        }







    }
}
