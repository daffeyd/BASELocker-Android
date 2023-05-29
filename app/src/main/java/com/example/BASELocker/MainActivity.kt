package com.example.BASELocker


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.example.aplikasi.R
import com.example.httpreq.RequestModel
import com.example.httpreq.ResponseModel
import com.example.httpreq.ServiceBuilder
import com.google.firebase.database.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    data class Locker(val campus: Campus)

    data class Campus(val location: Location)

    data class Location(val name: Name)

    data class Name(val status: String, val prediction: String)


    private lateinit var campusLocationEditText: EditText
    private lateinit var roomLocationEditText: EditText
    private lateinit var lockerNumberEditText: EditText
    private lateinit var textView2: TextView

    private val activity1ResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val campusLocation = data?.getStringExtra("campusLocation")
                val roomLocation = data?.getStringExtra("roomLocation")
                val lockerNumber = data?.getStringExtra("lockerNumber")

                campusLocationEditText.setText(campusLocation)
                roomLocationEditText.setText(roomLocation)
                lockerNumberEditText.setText(lockerNumber)
            }
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
    fun getLockerPath(lockerRef: DatabaseReference, onStatusValue: (String, String?) -> Unit) {
        lockerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot. getValue()?.toString()
                if (value != null) {
                    Log.i("string value", value)
                    val elements = value.split(";")
                    val campus = elements[0]
                    val location = elements[1]
                    onStatusValue(campus,location)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("fail", "Failed to read value.", error.toException())
            }
        })
    }
    fun makeApiRequest(requestModel: RequestModel, callback: (responseData: ResponseModel?) -> Unit) {
        val response = ServiceBuilder.buildService(ApiRecomendationInterface::class.java)
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
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val database = FirebaseDatabase.getInstance()
        val reference = FirebaseDatabase.getInstance().getReference();
        val username = intent.getStringExtra("username")
        val userRef = database.getReference("user/$username")


        getUsernameandPassValue(userRef) { pass, locker ->
            if (locker != "-"){
                val intent = Intent(this, activity1::class.java)
                intent.putExtra("username", username)
                startActivity(intent)
            }
            else{
                //
                val intent = Intent(this, activity2::class.java)
                intent.putExtra("username", username)
                val items = arrayOf("BINUS ASO")
                val autoComplete: AutoCompleteTextView = findViewById(R.id.AutoComplete)
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, items)

                autoComplete.setAdapter(adapter)


                autoComplete.onItemClickListener =
                    AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        val itemSelected = adapterView.getItemAtPosition(i)

                        intent.putExtra("campus", "$itemSelected")
                        Log.i("dropdown Campus ","$itemSelected" )
                        Toast.makeText(this, "Campus Location: $itemSelected", Toast.LENGTH_SHORT).show()
                    }


                val itemsRoom = arrayOf("Main Hallway")
                val autoCompleteRoom: AutoCompleteTextView = findViewById(R.id.AutoCompleteRoom)
                val adapterRoom = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, itemsRoom)
                autoCompleteRoom.setAdapter(adapterRoom)


                autoCompleteRoom.onItemClickListener =
                    AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        val itemSelected = adapterView.getItemAtPosition(i)
                        intent.putExtra("location", "$itemSelected")
                        Log.i("dropdown location ","$itemSelected" )
                        Toast.makeText(this, "Room Location: $itemSelected", Toast.LENGTH_SHORT).show()
                    }

                val itemsLoc = arrayOf("A", "C")
                val autoCompleteLoc: AutoCompleteTextView = findViewById(R.id.AutoCompleteLocker)
                val adapterLoc = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, itemsLoc)
                autoCompleteLoc.setAdapter(adapterLoc)

                autoCompleteLoc.onItemClickListener =
                    AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        val itemSelected = adapterView.getItemAtPosition(i)
                        intent.putExtra("name", "$itemSelected")
                        Log.i("dropdown name ","$itemSelected" )
                        Toast.makeText(this, "Locker Name: $itemSelected", Toast.LENGTH_SHORT).show()
                    }


                //
                val usernameTextView = findViewById<TextView>(R.id.textView2)
                val helloText = "Hello, "


                val combinedText = "$helloText$username"
                usernameTextView.text = combinedText


                val showStatusButton = findViewById<Button>(R.id.button)
                showStatusButton.setOnClickListener {

                    startActivity(intent)
                    finish()
                }

                val currentDateTime = LocalDateTime.now()
                val formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("EEEE, HH:mm:ss"))

                val day = formattedDateTime?.substringBefore(",").toString()
                val currentTime = LocalTime.now()

                val category = when {
                    currentTime >= LocalTime.parse("00:00") && currentTime < LocalTime.parse("10:10") -> 1
                    currentTime >= LocalTime.parse("10:10") && currentTime < LocalTime.parse("12:00") -> 2
                    currentTime >= LocalTime.parse("12:00") && currentTime < LocalTime.parse("15:00") -> 3
                    currentTime >= LocalTime.parse("15:00") && currentTime < LocalTime.parse("17:00") -> 4
                    else -> 5
                }

                println("Category: $category")
                println("Current day of the week: $day")
                val requestModel = RequestModel(day, category)
                val conditionalButton = findViewById<Button>(R.id.conditionalButton)
                makeApiRequest(requestModel) { responseData ->
                    if (responseData != null) {
                        conditionalButton.visibility = View.VISIBLE
                        conditionalButton.isEnabled = true
                        // Handle the successful response
                        val predictedValue = responseData.toString().substringAfter("=").substringBefore(")")
                        Log.i("predict",predictedValue )
                        val lockerRef = database.getReference("databases/lockerPath/$predictedValue")
                        getLockerPath(lockerRef) { campus ,location ->

                            val recommendation = "Recommendation Locker:"
                            val campusText = "Campus: $campus"
                            val locationText = "Location: $location"
                            val lockerNameText = "Locker Name: $predictedValue"
                            val recommendationText = "$recommendation\n$campusText\n$locationText\n$lockerNameText"
                            conditionalButton.text = recommendationText
                            conditionalButton.setOnClickListener{
                                intent.putExtra("campus", campus)
                                intent.putExtra("location", location)
                                intent.putExtra("name", predictedValue)
                                startActivity(intent)
                                finish()
                            }


                        }


                    } else {
                        // Handle the failure or null response
                        Log.i("failure", "API request failed or received null response")
                        conditionalButton.visibility = View.GONE
                        conditionalButton.isEnabled = false
                    }
                }

            }

        }
    }
}
