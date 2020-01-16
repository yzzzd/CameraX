package com.nuryazid.camerax.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nuryazid.camerax.R
import com.nuryazid.camerax.data.constant.Constants
import com.nuryazid.camerax.util.ClickPrevention
import kotlinx.android.synthetic.main.activity_camera_permission.*

class CameraPermissionActivity : AppCompatActivity(), ClickPrevention {

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_permission)
        setResult(Constants.RES.CAMERA)
        btnOK.setOnClickListener(this)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission() {
        if (allPermissionsGranted()) {
            finish()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, Constants.REQ.CAMERA)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.REQ.CAMERA) {
            if (allPermissionsGranted()) {
                finish()
            } else {
                checkPermission()
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnOK -> checkPermission()
        }
        super.onClick(v)
    }
}
