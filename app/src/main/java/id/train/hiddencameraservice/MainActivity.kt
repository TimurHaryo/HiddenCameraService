package id.train.hiddencameraservice

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import id.train.hiddencameraservice.service.CameraBroadcast
import id.train.hiddencameraservice.service.CameraService
import id.train.hiddencameraservice.service.ICameraCallback
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), ICameraCallback {
    private val TAG = this.javaClass.simpleName
    private var isRunning = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupView()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupView() {
        CameraService.mICameraCallback = this
        checkPermission()
        btn_service.setOnClickListener {
            if (!isRunning) {
                startHiddenCamera()
            } else {
                stopHiddenCamera()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION
                )
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.message.toString())
        }
    }

    private fun startHiddenCamera() {
        isRunning = true
        btn_service.text = "stop"
        val alarm = Intent(this, CameraBroadcast::class.java)
//        startService(alarm)
        if (!isServiceRunning(alarm)) {
            val interval = 1000L
            val pendingIntent = PendingIntent.getBroadcast(this, 0, alarm, 0)
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), interval, pendingIntent)
        }
    }

    private fun stopHiddenCamera() {
        isRunning = false
        btn_service.text = "go"
        Toast.makeText(this, "See you then", Toast.LENGTH_SHORT).show()
        stopService(Intent(this, CameraService::class.java))
    }

    private fun isServiceRunning(alarm: Intent) : Boolean {
        return PendingIntent.getBroadcast(this, 0, alarm, PendingIntent.FLAG_NO_CREATE) != null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT).show()
            } else {
                startHiddenCamera()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun requestCameraPermission(permission: String) {
//        try {
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.CAMERA
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                requestPermissions(
//                    arrayOf(Manifest.permission.CAMERA),
//                    CAMERA_PERMISSION
//                )
//            }
//        } catch (e: CameraAccessException) {
//            Log.e(TAG, e.message.toString())
//        }
        Toast.makeText(this, "Camera permission not available", Toast.LENGTH_SHORT).show()
    }

    override fun onPhotoCaptured(imageFile: File) {
        Glide.with(this)
            .load(imageFile)
            .into(iv_captured_img)

        Toast.makeText(this, "I see you", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val CAMERA_PERMISSION = 1000
    }
}