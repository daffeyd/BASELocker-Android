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
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class PhoneRaiseDetector(private val context: Context) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var raiseListener: PhoneRaiseListener? = null

    // Interface for receiving phone raise events
    interface PhoneRaiseListener {
        fun onPhoneRaised()
        fun onPhoneLowered()
    }

    // Register the phone raise listener
    fun setPhoneRaiseListener(listener: PhoneRaiseListener) {
        raiseListener = listener
    }

    // Start listening for phone raise events
    fun startListening() {
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Stop listening for phone raise events
    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used in this example
    }
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { sensorEvent ->
            if (sensorEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = sensorEvent.values[0]
                val y = sensorEvent.values[1]
                val z = sensorEvent.values[2]
//
//                Log.d("updown", "$y",)
//                Log.d("rightLeft", "$x",)

                // Calculate the magnitude of the acceleration vector
                val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble())

                // Adjust these threshold values as per your requirement
                val raiseThreshold = 10 // Threshold for raising the phone
                val lowerThreshold = -15 // Threshold for lowering the phone
                if (y.toInt() > 8) {

                    raiseListener?.onPhoneRaised()
                   }

                }
            }
        }
    }


class MainActivity : AppCompatActivity() {

    private lateinit var campusLocationEditText: EditText
    private lateinit var roomLocationEditText: EditText
    private lateinit var lockerNumberEditText: EditText

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
    fun getLockerArray(lockerRef: DatabaseReference, onArrayValue: (Array<String>) -> Unit) {
        lockerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot. getValue()?.toString()

                Log.i("dropdown location ", "$value")
                val array = value?.split(";")?.toTypedArray()
                Log.i("dropdown location ", "$array")
                // Invoke the onArrayValue callback with the array value
                if (array != null) {
                    onArrayValue(array)
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
    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onBackPressed() {
        // Disable the back button functionality
        // Uncomment the line below if you want to block the back button completely
        // super.onBackPressed()

        // Or do nothing to simply ignore the back button press
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val database = FirebaseDatabase.getInstance()
        val username = intent.getStringExtra("username")
        val userRef = database.getReference("user/$username")


        getUsernameandPassValue(userRef) { pass, locker ->

            if (locker != "-"){
                val intentNone = Intent(this, activity1::class.java)
                intentNone.putExtra("username", username)
                startActivity(intentNone)
            }
            else{
                val intentQR = Intent(this, qrReader::class.java)

                val phoneRaiseDetector = PhoneRaiseDetector(this)

                phoneRaiseDetector.setPhoneRaiseListener(object : PhoneRaiseDetector.PhoneRaiseListener {
                    override fun onPhoneRaised() {
                        phoneRaiseDetector.stopListening()
                        intentQR.putExtra("username", username)
                        startActivity(intentQR)
                        Log.i("Phone Raised", "naiik")
                    }

                    override fun onPhoneLowered() {
                        Log.i("Phone Lowered", "Turun")
                    }
                })
                phoneRaiseDetector.startListening()



                val intent = Intent(this, activity2::class.java)
                intent.putExtra("username", username)
                val campusArrayRef = database.getReference("databases/campus")


                getLockerArray(campusArrayRef) { array ->
                    Log.d("array value", "$array")

                val items = array
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
                }

                val locationArrayRef = database.getReference("databases/location")

                getLockerArray(locationArrayRef) { array ->
                    val itemsRoom = array
                    val autoCompleteRoom: AutoCompleteTextView = findViewById(R.id.AutoCompleteRoom)
                    val adapterRoom =
                        ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, itemsRoom)
                    autoCompleteRoom.setAdapter(adapterRoom)

                    autoCompleteRoom.onItemClickListener =
                        AdapterView.OnItemClickListener { adapterView, view, i, l ->
                            val itemSelected = adapterView.getItemAtPosition(i)
                            intent.putExtra("location", "$itemSelected")
                            Log.i("dropdown location ", "$itemSelected")
                            Toast.makeText(this, "Room Location: $itemSelected", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                val nameArrayRef = database.getReference("databases/name")

                getLockerArray(nameArrayRef) { array ->
                    val itemsLoc = array
                    val autoCompleteLoc: AutoCompleteTextView =
                        findViewById(R.id.AutoCompleteLocker)
                    val adapterLoc =
                        ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, itemsLoc)
                    autoCompleteLoc.setAdapter(adapterLoc)

                    autoCompleteLoc.onItemClickListener =
                        AdapterView.OnItemClickListener { adapterView, view, i, l ->
                            val itemSelected = adapterView.getItemAtPosition(i)
                            intent.putExtra("name", "$itemSelected")
                            Log.i("dropdown name ", "$itemSelected")
                            Toast.makeText(this, "Locker Name: $itemSelected", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                //
                val usernameTextView = findViewById<TextView>(R.id.textView2)
                val helloText = "Hello, "


                val combinedText = "$helloText$username"
                usernameTextView.text = combinedText


                val showStatusButton = findViewById<Button>(R.id.button)
                showStatusButton.setOnClickListener {
                    phoneRaiseDetector.stopListening()
                    startActivity(intent)
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
                                phoneRaiseDetector.stopListening()
                                startActivity(intent)
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
