package com.example.BASELocker


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.example.aplikasi.R
import com.google.firebase.database.*

class activity1 : AppCompatActivity() {
    private lateinit var countdownTextView: TextView
    private lateinit var countdown24HoursTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var countDownTimer24Hours: CountDownTimer

    fun getUsernameandPassValue(lockerRef: DatabaseReference, onStatusValue: (String, String?) -> Unit) {
        lockerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot. getValue()?.toString()
                if (value != null) {
                    Log.i("string value", value)
                }

                val pass = value?.substringAfter("password=")?.substringBefore(",").toString()
                val locker = value?.substringAfter("locker=")?.substringBefore("}").toString()
                Log.i("string locker", locker)
                onStatusValue(pass,locker)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("fail", "Failed to read value.", error.toException())

            }
        })
    }
    fun getStatusandPredValue(lockerRef: DatabaseReference, onStatusValue: (String,String?) -> Unit) {
        lockerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val statusValue = dataSnapshot.getValue()?.toString()
//                val gson = Gson()
//                val locker = gson.fromJson(statusValue, locker::class.java)
                val pred = statusValue?.substringAfter("predReturn=")?.substringBefore(",")
                val status = statusValue?.substringAfter("status=")?.substringBefore("}")


                if (status != null) {
                    onStatusValue(status, pred)
                }

//
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("fail", "Failed to read value.", error.toException())

            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_1)

        val username = intent.getStringExtra("username")
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("user/$username")


        getUsernameandPassValue(userRef) { pass, locker ->

            if (locker!="-"){
            countdownTextView = findViewById(R.id.countdownTextView)
            countdown24HoursTextView = findViewById(R.id.countdown24HoursTextView)

            val openButton: Button = findViewById(R.id.button_Open)
            val closeButton: Button = findViewById(R.id.button_Close)
            val returnButton: Button = findViewById(R.id.button_Return)
            val imagelock: ImageView = findViewById(R.id.imageView)

                val campus = locker?.substringAfter("campus=")?.substringBefore(";").toString()
                val location = locker?.substringAfter("location=")?.substringBefore(";").toString()
                val name = locker?.substringAfter("name=")?.substringBefore(";").toString()
                val number = locker?.substringAfter("number=")?.substringBefore(";").toString()
                val limitTime = locker?.substringAfter("limit=")?.substringBefore(";")?.toLong()
                val returnTime = locker?.substringAfter("time=")?.toLong()
                Log.i("breakdown lo", "$campus , $location, $name, $number, $returnTime ")

                // Display the values in TextViews or any other UI components
                val usernameTextView = findViewById<TextView>(R.id.Hello)
                val campusLocationTextView = findViewById<TextView>(R.id.campusLocationTextView)
                val roomLocationTextView = findViewById<TextView>(R.id.roomLocationTextView)
                val lockerNameTextView = findViewById<TextView>(R.id.lockerNameTextView)
                val lockerNumberTextView = findViewById<TextView>(R.id.lockerNumberTextView)




                usernameTextView.text = "Hello, $username"
                campusLocationTextView.text = campus
                roomLocationTextView.text = location
                lockerNameTextView.text = name
                lockerNumberTextView.text = number



            val remainingTime = returnTime!! - System.currentTimeMillis()

            countDownTimer24Hours = object : CountDownTimer(remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val seconds = (millisUntilFinished / 1000) % 60
                    val minutes = (millisUntilFinished / (1000 * 60)) % 60
                    val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                    countdown24HoursTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }

                override fun onFinish() {
                    println("Timer expired! 24 hours have passed.")
                }
            }

            countDownTimer24Hours.start()
            val remainingLimitTime = limitTime!! - System.currentTimeMillis()

            if (remainingLimitTime > 0) {
                countDownTimer = object : CountDownTimer(remainingLimitTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val seconds = (millisUntilFinished / 1000) % 60
                        val minutes = (millisUntilFinished / (1000 * 60)) % 60
                        val hours = (millisUntilFinished / (1000 * 60 * 60)) % 24
                        countdownTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)


                    }

                    override fun onFinish() {
                        println("Timer expired! 3 minutes have passed.")
                        val lockerRef = database.getReference("locker/$campus/$location/$name")
                        getStatusandPredValue(lockerRef) { status, pred ->
                            // Use the statusValue here
                            var index = 0
                            index = number.toInt()
                            Log.i("status dan index", "$status , $index")

                            val statusNumber = status[index-1].toString().toInt()


                            Log.i("status number", "$statusNumber")
                            if (statusNumber > 1){
                                countDownTimer.cancel()
                            }
                            else {
                                val statusValueUpdate =
                                    status.substring(0, index - 1) + "0" + status.substring(index)
                                val lockerRefUpdate =
                                    database.getReference("locker/$campus/$location/$name/status")
                                Log.i("status number", "kenapa di timer")
                                lockerRefUpdate.setValue(statusValueUpdate)
                                val userLocker = "-"
                                val userLockerRef = database.getReference("user/$username/locker")

                                userLockerRef.setValue(userLocker)
                            }



                        }

                }}

                countDownTimer.start()

            }

                Log.i("status number", "$remainingLimitTime")
                if (remainingLimitTime <= 0 ){
                    Log.i("status number", "kenapa")
                    val lockerRef = database.getReference("locker/$campus/$location/$name")
                    getStatusandPredValue(lockerRef) { status, pred ->
                        // Use the statusValue here
                        var index = 0
                        index = number.toInt()
                        Log.i("status dan index", "$status , $index")

                        val statusNumber = status[index-1].toString().toInt()


                        Log.i("status number", "$statusNumber")
                        if (statusNumber > 1){
                            countDownTimer.cancel()
                        }
                        else {
                            Log.i("status number", "kenapa")
                            val statusValueUpdate = status.substring(0, index - 1) + "0" + status.substring(index)
                            val lockerRefUpdate =
                                database.getReference("locker/$campus/$location/$name/status")
                            lockerRefUpdate.setValue(statusValueUpdate)
                            val userLocker = "-"
                            val userLockerRef = database.getReference("user/$username/locker")

                            userLockerRef.setValue(userLocker)
                        }

                    }

            }

                val lockerRef = database.getReference("locker/$campus/$location/$name")
                getStatusandPredValue(lockerRef) { status, pred ->
                    // Use the statusValue here
                    var index = 0
                    index = number.toInt()
                    Log.i("status dan index", "$status , $index")


                

// Start the countdown when the "Open" button is pressed
                openButton.setOnClickListener {
                    imagelock.setImageResource(R.mipmap.unlocked_padlock)
                    val statusValueUpdate = status.substring(0, index - 1) + "4" + status.substring(index)
                    val lockerRefUpdate = database.getReference("locker/$campus/$location/$name/status")
                    lockerRefUpdate.setValue(statusValueUpdate)


                }

                // Cancel the countdown timers when the "Close" button is pressed
                closeButton.setOnClickListener {
                    imagelock.setImageResource(R.drawable.bitmap2x)

                        val statusValueUpdate = status.substring(0, index - 1) + "3" + status.substring(index)
                        val lockerRefUpdate = database.getReference("locker/$campus/$location/$name/status")
                        lockerRefUpdate.setValue(statusValueUpdate)


                }

                returnButton.setOnClickListener {
                    // Create an Intent to go back to MainActivity



                        val statusValueUpdate = status.substring(0, index-1) + "0" + status.substring(index)
                        val lockerRefUpdate = database.getReference("locker/$campus/$location/$name/status")
                        Log.i("status number", "kenapa di return")
                        lockerRefUpdate.setValue(statusValueUpdate)
                    val userLocker = "-"
                    val userLockerRef = database.getReference("user/$username/locker")
                    userLockerRef.setValue(userLocker)


                }
            }
        }

            else{
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
                finish()
            }

            }



    }


}


