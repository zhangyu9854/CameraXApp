package com.example.cameraxapp

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cameraxapp.utils.Constants.REQUEST_CODE_CAMERA
import com.example.cameraxapp.utils.Constants.REQUEST_CODE_CROP
import com.example.cameraxapp.utils.FileManager
import com.example.cameraxapp.utils.FileUtil
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File


class CameraActivity : AppCompatActivity() {

    private var mUploadImageUri: Uri? = null
    private var mUploadImageFile: File? = null
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        initView()
    }

    private fun initView() {
        btnCamera.setOnClickListener {
            startSystemCamera()
        }
    }

    /**
     * 调起系统相机拍照
     */
    private fun startSystemCamera() {
        val takeIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val values = ContentValues()
        //根据uri查询图片地址
        photoUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        Log.w("lzq", "photoUri:" + photoUri?.authority + ",photoUri:" + photoUri?.path)
        //放入拍照后的地址
        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        //调起拍照
        startActivityForResult(
            takeIntent,
            REQUEST_CODE_CAMERA
        )
    }


    /**
     * 设置用户头像
     */
    private fun setAvatar() {
        val file: File? = if (mUploadImageUri != null) {
            FileManager.getMediaUri2File(mUploadImageUri)
        } else {
            mUploadImageFile
        }
        Glide.with(this).load(file).into(iv_avatar)
        Log.d("filepath", file!!.absolutePath)
    }

    /**
     * 系统裁剪方法
     */
    private fun workCropFun(imgPathUri: Uri?) {
        mUploadImageUri = null
        mUploadImageFile = null
        if (imgPathUri != null) {
            val imageObject: Any = FileUtil.getHeadJpgFile()
            if (imageObject is Uri) {
                mUploadImageUri = imageObject
            }
            if (imageObject is File) {
                mUploadImageFile = imageObject
            }
            val intent = Intent("com.android.camera.action.CROP")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            intent.run {
                setDataAndType(imgPathUri, "image/*")// 图片资源
                putExtra("crop", "true") // 裁剪
                putExtra("aspectX", 1) // 宽度比
                putExtra("aspectY", 1) // 高度比
                putExtra("outputX", 150) // 裁剪框宽度
                putExtra("outputY", 150) // 裁剪框高度
                putExtra("scale", true) // 缩放
                putExtra("return-data", false) // true-返回缩略图-data，false-不返回-需要通过Uri
                putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()) // 保存的图片格式
                putExtra("noFaceDetection", true) // 取消人脸识别
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    putExtra(MediaStore.EXTRA_OUTPUT, mUploadImageUri)
                } else {
                    val imgCropUri = Uri.fromFile(mUploadImageFile)
                    putExtra(MediaStore.EXTRA_OUTPUT, imgCropUri)
                }
            }
            startActivityForResult(
                intent, REQUEST_CODE_CROP
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {//拍照回调
                workCropFun(photoUri)
            } else if (requestCode == REQUEST_CODE_CROP) {//裁剪回调
                setAvatar()
            }
        }
    }
}