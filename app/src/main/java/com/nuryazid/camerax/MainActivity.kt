package com.nuryazid.camerax

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.nuryazid.camerax.camera.CameraActivity
import com.nuryazid.camerax.data.constant.Constants
import com.nuryazid.camerax.util.ClickPrevention
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), ClickPrevention {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button -> startActivityForResult(Intent(this, CameraActivity::class.java), Constants.REQ.PHOTO)
        }
        super.onClick(v)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.REQ.PHOTO) {
            if (resultCode == Constants.RES.PHOTO) {
                data?.getStringExtra(Constants.BUNDLE.DATA)?.let {
                    imageView.setImageURI(Uri.fromFile(File(it)))
                }
            }
        }
    }
}
