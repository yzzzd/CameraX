package com.nuryazid.camerax.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureConfig
import androidx.camera.core.PreviewConfig
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.nuryazid.camerax.R
import com.nuryazid.camerax.data.constant.Constants
import com.nuryazid.camerax.permission.CameraPermissionActivity
import com.nuryazid.camerax.util.ClickPrevention
import com.nuryazid.camerax.util.camera.AutoFitPreviewBuilder
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(), ClickPrevention, LifecycleOwner {

    private var imageCapture: ImageCapture? = null

    private val REQUIRED_PERMISSIONS_CAMERA = arrayOf(Manifest.permission.CAMERA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_camera)

        // Request camera permissions
        if (cameraGranted()) {
            viewFinder.post { startCamera() }
        } else {
            startActivityForResult(Intent(this, CameraPermissionActivity::class.java), Constants.REQ.CAMERA)
        }

        // Every time the provided texture view changes, recompute layout
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        btnBack.setOnClickListener(this)
        btnCamera.setOnClickListener(this)
    }

    // Add this after onCreate

    private val executor = Executors.newSingleThreadExecutor()

    private fun startCamera() {
        /*// Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640, 480))
            setLensFacing(CameraX.LensFacing.FRONT)
        }.build()

        // Build the viewfinder use case
        val preview = Preview(previewConfig)*/

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            //setTargetAspectRatio(screenAspectRatio)
            //setTargetResolution(Size(metrics.widthPixels, metrics.widthPixels))
            //setTargetRotation(viewFinder.display.rotation)
        }.build()

        // Use the auto-fit preview builder to automatically handle size and orientation changes
        //val preview = Preview(previewConfig)
        val preview = AutoFitPreviewBuilder.build(previewConfig, viewFinder)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                // We don't set a resolution for image capture; instead, we
                // select a capture mode which will infer the appropriate
                // resolution based on aspect ration and requested mode
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                setLensFacing(CameraX.LensFacing.BACK)
            }.build()

        // Build the image capture use case and attach button click listener
        imageCapture = ImageCapture(imageCaptureConfig)


        // Bind use cases to lifecycle
        // If Android Studio complains about "this" being not a LifecycleOwner
        // try rebuilding the project or updating the appcompat dependency to
        // version 1.1.0 or higher.
        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnBack -> onBackPressed()
            R.id.btnCamera -> {

                val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

                imageCapture?.takePicture(file, executor,
                    object : ImageCapture.OnImageSavedListener {
                        override fun onError(
                            imageCaptureError: ImageCapture.ImageCaptureError,
                            message: String,
                            exc: Throwable?
                        ) {
                            val msg = "Photo capture failed: $message"
                            viewFinder.post { snacked(rootView, msg) }
                        }

                        override fun onImageSaved(file: File) {
                            val msg = "Photo capture succeeded: ${file.absolutePath}"
                            viewFinder.post {
                                intent.putExtra(Constants.BUNDLE.DATA, file.absolutePath)
                                setResult(Constants.RES.PHOTO, intent)
                                finish()
                            }
                        }
                    })
            }
        }
        super.onClick(v)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.REQ.CAMERA && resultCode == Constants.RES.CAMERA) {
            if (cameraGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this, getString(R.string.alert_permission_not_granted), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun cameraGranted() = REQUIRED_PERMISSIONS_CAMERA.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    fun snacked(view: View, message: String?, duration: Int = Snackbar.LENGTH_SHORT) {
        message?.let { Snackbar.make(view, it, duration).show() }
    }
}