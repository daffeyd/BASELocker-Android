package com.example.BASELocker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.example.aplikasi.databinding.QrreaderBinding


class qrReader : AppCompatActivity() {
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val username = intent.getStringExtra("username")
        val intentMain = Intent(this, MainActivity::class.java)
        intentMain.putExtra("username", username)
        startActivity(intentMain)
    }

    private lateinit var binding: QrreaderBinding
    private lateinit var codeScanner: CodeScanner


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QrreaderBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupPermissions()
        codeScanner()
    }

    private fun codeScanner() {
        val username = intent.getStringExtra("username")
        val intentAct2 = Intent(this, lockerNumberSelector::class.java)
        codeScanner = CodeScanner(this, binding.scn)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false


            decodeCallback = DecodeCallback { result ->
                runOnUiThread {
                    binding.tvText.text = "Scanning.."
                    val scannedValue = result.text
                    val campus = scannedValue?.substringAfter("campus=")?.substringBefore(",")
                    val location = scannedValue?.substringAfter("location=")?.substringBefore(",")
                    val name = scannedValue?.substringAfter("name=")
                    if (campus.equals(location) && location.equals(name)){
                        binding.tvText.text = "Please scan QR Code near the designated lockers"

                    }
                    else{

                        intentAct2.putExtra("username", username)
                        intentAct2.putExtra("campus", campus)
                        intentAct2.putExtra("location", location)
                        intentAct2.putExtra("name", name)
                        Log.d("qrReader", "Scanned value: $campus,$location,$name")
                        startActivity(intentAct2)
                    }


//                    binding.tvText.text = scannedValue
//                    Log.d("qrReader", "Scanned value: $scannedValue")
                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("Main", "codeScanner: ${it.message}")
                }
            }

            binding.scn.setOnClickListener {
                codeScanner.startPreview()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQ
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )
    {super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQ -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "You need the camera permission to use this app",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        private const val CAMERA_REQ = 101
    }
}