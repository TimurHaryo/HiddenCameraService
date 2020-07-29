package id.train.hiddencameraservice.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidhiddencamera.CameraConfig
import com.androidhiddencamera.CameraError
import com.androidhiddencamera.HiddenCameraService
import com.androidhiddencamera.HiddenCameraUtils
import com.androidhiddencamera.config.CameraFacing
import com.androidhiddencamera.config.CameraImageFormat
import com.androidhiddencamera.config.CameraResolution
import com.androidhiddencamera.config.CameraRotation
import id.train.hiddencameraservice.R
import id.train.hiddencameraservice.utils.launchIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext


class CameraService : HiddenCameraService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        launchIO { captureSecretly() }

        return Service.START_STICKY
    }

    private suspend fun captureSecretly() {
        if (ActivityCompat.checkSelfPermission(
                this@CameraService,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (HiddenCameraUtils.canOverDrawOtherApps(this@CameraService)) {
                val cameraConfig = CameraConfig()
                    .getBuilder(this@CameraService)
                    .setCameraFacing(CameraFacing.FRONT_FACING_CAMERA)
                    .setCameraResolution(CameraResolution.MEDIUM_RESOLUTION)
                    .setImageFormat(CameraImageFormat.FORMAT_JPEG)
                    .setImageRotation(CameraRotation.ROTATION_270)
                    .build()

                withContext(Dispatchers.Main) {
                    startCamera(cameraConfig)
                    Handler().postDelayed({
                        takePicture()
                    }, 1000L)
                }
            } else {
                HiddenCameraUtils.openDrawOverPermissionSetting(this@CameraService)
            }
        } else {
            mICameraCallback?.requestCameraPermission(Manifest.permission.CAMERA)
        }
    }

    override fun onCameraError(errorCode: Int) {
        when (errorCode) {
            CameraError.ERROR_CAMERA_OPEN_FAILED ->
                //Camera open failed. Probably because another application
                //is using the camera
                Toast.makeText(this, R.string.error_cannot_open, Toast.LENGTH_LONG).show()
            CameraError.ERROR_IMAGE_WRITE_FAILED ->
                //Image write failed. Please check if you have provided WRITE_EXTERNAL_STORAGE permission
                Toast.makeText(this, R.string.error_cannot_write, Toast.LENGTH_LONG).show()
            CameraError.ERROR_CAMERA_PERMISSION_NOT_AVAILABLE ->
                //camera permission is not available
                //Ask for the camera permission before initializing it.
                Toast.makeText(this, R.string.error_cannot_get_permission, Toast.LENGTH_LONG).show()
            CameraError.ERROR_DOES_NOT_HAVE_OVERDRAW_PERMISSION ->
                //Display information dialog to the user with steps to grant "Draw over other app"
                //permission for the app.
                HiddenCameraUtils.openDrawOverPermissionSetting(this)
            CameraError.ERROR_DOES_NOT_HAVE_FRONT_CAMERA -> Toast.makeText(
                this,
                R.string.error_not_having_camera,
                Toast.LENGTH_LONG
            ).show()
        }

        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onImageCapture(imageFile: File) {
        Log.v("PHOTO", imageFile.path.toString())
        mICameraCallback?.onPhotoCaptured(imageFile)
//        stopCamera()
//        stopSelf()
    }

    companion object {
        var mICameraCallback: ICameraCallback? = null
    }
}