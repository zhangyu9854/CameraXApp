package com.example.cameraxapp.utils

import android.content.Context
import android.os.Environment
import com.example.cameraxapp.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    /**
     * 获取视频文件路径
     */
    fun getVideoName(): String {
        val videoPath = Environment.getExternalStorageDirectory().toString() + "/CameraXApp"
        val dir = File(videoPath)
        if (!dir.exists() && !dir.mkdirs()) {
            ToastUtils.shortToast("文件不存在")
        }
        return videoPath
    }

    /**
     * 获取图片文件路径
     */
    fun getImageFileName(): String {
        val imagePath = Environment.getExternalStorageDirectory().toString() + "/images"
        val dir = File(imagePath)
        if (!dir.exists() && !dir.mkdirs()) {
            ToastUtils.shortToast("文件不存在")
        }
        return imagePath
    }

    /**
     * 拍照文件保存路径
     * @param context
     * @return
     */
    fun getPhotoDir(context: Context?): String? {
        return FileManager.getFolderDirPath(
            "DCIM/Camera/CameraXApp/photo"
        )
    }

    /**
     * 视频文件保存路径
     * @param context
     * @return
     */
    fun getVideoDir(): String? {
        return FileManager.getFolderDirPath(
            "DCIM/Camera/CameraXApp/video"
        )
    }

    /** Use external media if it is available, our app's file directory otherwise */
    fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

   fun createFile(baseFolder: File, format: String, extension: String) =
        File(
            baseFolder, SimpleDateFormat(format, Locale.US)
                .format(System.currentTimeMillis()) + extension
        )
}