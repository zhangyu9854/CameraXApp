package com.example.cameraxapp.utils

import android.Manifest

/**
 * @auth: zy
 * @desc: 常量管理工具类
 */
object Constants {
    const val REQUEST_CODE_PERMISSIONS = 101
    const val REQUEST_CODE_CAMERA = 102
    const val REQUEST_CODE_CROP = 103

    const val DATE_FORMAT = "yyyy-MM-dd HH.mm.ss"
    const val PHOTO_EXTENSION = ".jpg"

    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )
}