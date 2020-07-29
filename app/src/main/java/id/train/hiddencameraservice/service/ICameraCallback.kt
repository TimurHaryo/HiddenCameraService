package id.train.hiddencameraservice.service

import java.io.File

interface ICameraCallback {
    fun requestCameraPermission(permission: String)
    fun onPhotoCaptured(imageFile: File)
}