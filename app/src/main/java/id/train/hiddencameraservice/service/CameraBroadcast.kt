package id.train.hiddencameraservice.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CameraBroadcast:  BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val intentService = Intent(context, CameraService::class.java)
        context?.startService(intentService)
    }
}