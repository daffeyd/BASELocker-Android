package com.example.BASELocker

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.aplikasi.R
import com.example.httpreq.RequestModel
import com.example.httpreq.ResponseModel
import com.example.httpreq.ServiceBuilder
import com.google.firebase.database.*
import kotlinx.coroutines.*
import okhttp3.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class activity2 : AppCompatActivity() {

    private lateinit var buttons: List<Button>



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)
        val database = FirebaseDatabase.getInstance()
        val username = intent.getStringExtra("username")
        val campus = intent.getStringExtra("campus")
        val location = intent.getStringExtra("location")
        val name = intent.getStringExtra("name")

        val userRef = database.getReference("user/$username")
        buttons = listOf(
            findViewById(R.id.Button1),
            findViewById(R.id.Button2),
            findViewById(R.id.Button3),
            findViewById(R.id.Button4),
            findViewById(R.id.Button5),
            findViewById(R.id.Button6),
            findViewById(R.id.Button7),
            findViewById(R.id.Button8)
        )

        fun makeApiRequest(requestModel: RequestModel, callback: (responseData: ResponseModel?) -> Unit) {
            val response = ServiceBuilder.buildService(ApiInterface::class.java)
            response.sendReq(requestModel).enqueue(object : Callback<ResponseModel> {
                override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                    if (response.isSuccessful) {
                        val responseData: ResponseModel? = response.body()
                        callback(responseData)
                    } else {
                        callback(null)
                    }
                }
                override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                    callback(null)
                }
            })
        }
        fun getStatusandPredValue(lockerRef: DatabaseReference, onStatusValue: (String,String?) -> Unit) {
            lockerRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val statusValue = dataSnapshot. getValue()?.toString()
//                val gson = Gson()
//                val locker = gson.fromJson(statusValue, locker::class.java)
                    val pred = statusValue?.substringAfter("predReturn=")?.substringBefore(",")
                    val status = statusValue?.substringAfter("status=")?.substringBefore("}")


                    if (status != null) {
                        onStatusValue(status,pred)
                    }

//
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("fail", "Failed to read value.", error.toException())

                }
            })
        }
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



        getUsernameandPassValue(userRef) { pass, locker ->
            if (locker != "-"){
                val intent = Intent(this, activity1::class.java)
                intent.putExtra("username", username)
                intent.putExtra("locker", locker)
                startActivity(intent)
                finish()

            }
            else{
                val myRef = database.getReference("message")
                val lockerRef = database.getReference("locker/$campus/$location/$name")

                getStatusandPredValue(lockerRef) { status, pred ->
                    // Use the statusValue here
                    buttons.forEachIndexed { index, button ->
                        val statusValueIndex = status.substring(index, index + 1).toInt()
                        if (statusValueIndex == 0) {
                            button.isEnabled = true
                            button.setBackgroundColor(ContextCompat.getColor(this@activity2,
                                R.color.green
                            ))
                            myRef.setValue("tombol hijau")
                        } else {
                            if (statusValueIndex >= 1) {
                                button.isEnabled = false
                                val indexText = index + 1


                                val predValueText = pred?.get(index).toString()
                                var conditionalText = ""
                                if (predValueText == "5"){
                                    conditionalText = "Predicted Returned\nAfter Shift 4"
                                }
                                else{
                                    conditionalText = "Predicted Returned :\nShift $predValueText"
                                }

                                val predReturnText = "$indexText\n $conditionalText"

                                button.text = predReturnText

                                button.setBackgroundColor(ContextCompat.getColor(this@activity2,
                                    R.color.red
                                ))
                                myRef.setValue("tombol merah")
                            }
                        }
                        button.setOnClickListener {
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Alert!")
                            builder.setMessage("Apakah yakin ingin memilih ${button.text}?")
                            builder.setPositiveButton("Yes") { dialog, which ->
                                var statusValueUpdate = status.substring(0, index) + "1" + status.substring(index + 1)
                                val lockerRefUpdate = database.getReference("locker/$campus/$location/$name/status")

                                lockerRefUpdate.setValue(statusValueUpdate)

                                val requestModel = RequestModel("Monday", 1)

                                makeApiRequest(requestModel) { responseData ->
                                    if (responseData != null) {
                                        // Handle the successful response
                                        responseData.toString()
                                        val predictedValue = responseData.toString().substringAfter("=").substringBefore(")")
                                        val predValueUpdate = pred?.substring(0, index) + predictedValue  + pred?.substring(index + 1)
                                        val predRef = database.getReference("locker/$campus/$location/$name/predReturn")
                                        predRef.setValue(predValueUpdate)
                                        Log.i("success", predictedValue)
                                        val number = index + 1

                                        val currentTimestamp = System.currentTimeMillis()
                                        val futureTimestamp = currentTimestamp + (24 * 60 * 60 * 1000) // Add 24 hours in milliseconds
                                        val limitTime = currentTimestamp + ( 60 * 1000) // Add 24 hours in milliseconds

                                        val userLocker = "campus=$campus;location=$location;name=$name;number=$number;limit=$limitTime;time=$futureTimestamp"
                                        val userLockerRef = database.getReference("user/$username/locker")
                                        userLockerRef.setValue(userLocker)

                                    } else {
                                        // Handle the failure or null response
                                        Log.i("failure", "API request failed or received null response")
                                    }
                                }

                                statusValueUpdate = null.toString()

                            }
                            builder.setNegativeButton("No") { dialog, which ->
                                // Do nothing
                                Log.d("myTag", "This is my message");
                            }
                            builder.show()
                        }
                    }
                }

            }

        }



}
}











