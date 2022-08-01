package com.example.cameraxapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.VideoCapture.OnVideoSavedCallback
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.example.cameraxapp.utils.Constants
import com.example.cameraxapp.utils.Constants.DATE_FORMAT
import com.example.cameraxapp.utils.Constants.PHOTO_EXTENSION
import com.example.cameraxapp.utils.Constants.REQUIRED_PERMISSIONS
import com.example.cameraxapp.utils.FileManager
import com.example.cameraxapp.utils.FileUtil
import com.example.cameraxapp.utils.FileUtils.createFile
import com.example.cameraxapp.utils.FileUtils.getOutputDirectory
import com.example.cameraxapp.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private var imageCamera: ImageCapture? = null
    private var cameraExecutor: ExecutorService? = null
    var videoCapture: VideoCapture? = null//录像用例
    var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA//当前相机
    var preview: Preview? = null//预览对象
    var cameraProvider: ProcessCameraProvider? = null//相机信息
    var camera: Camera? = null//相机对象
    var isRecordVideo: Boolean = false
    private val TAG = "CameraXApp"
    private lateinit var outputDirectory: File
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initPermission()
        outputDirectory = getOutputDirectory(this)
    }


    private fun initPermission() {
        if (allPermissionsGranted()) {
            // ImageCapture
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, Constants.REQUEST_CODE_PERMISSIONS
            )
        }
        btnCameraCapture.setOnClickListener {
            takePhoto()
        }
        btnVideo.setOnClickListener {
            btnVideo.text = "停止录像"
            takeVideo()
        }
        btnSwitch.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            if (!isRecordVideo) {
                startCamera()
            }
        }
        btnOpenCamera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }



    /**
     * 开始拍照
     */
    private fun takePhoto() {
        val imageCapture = imageCamera ?: return
        val photoFile = createFile(outputDirectory, DATE_FORMAT, PHOTO_EXTENSION)
        val metadata = ImageCapture.Metadata().apply {
            // 镜像
            isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
        }
  /*      val mFileForMat = SimpleDateFormat(DATE_FORMAT, Locale.US)
        val file = File(FileManager.getAvatarPath(mFileForMat.format(Date()) + ".jpg"))*/
        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(photoFile).setMetadata(metadata).build()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    ToastUtils.shortToast(" 拍照失败 ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                    ToastUtils.shortToast(" 拍照成功,地址是 $savedUri")
                    Log.d(TAG, savedUri.path.toString())
                    // 显示拍照内容
                    if(ivPic!=null){
                        ivPic.setImageBitmap(BitmapFactory.decodeFile(photoFile.getAbsolutePath()));
                    }
                    //获取相应的扩展名类型
                    val mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(savedUri.toFile().extension)
                    MediaScannerConnection.scanFile(
                        this@MainActivity,
                        arrayOf(savedUri.toFile().absolutePath),
                        arrayOf(mimeType)
                    ) { _, uri ->
                        Log.d(TAG, "Image capture scanned into media store: $uri")
                    }
                }
            })
    }


    /**
     * 开始录像
     */
    @SuppressLint("RestrictedApi", "ClickableViewAccessibility")
    private fun takeVideo() {
        isRecordVideo = true
        val mFileDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
        //视频保存路径
        val file = File(FileManager.getCameraVideoPath(), mFileDateFormat.format(Date()) + ".mp4")
        //开始录像
        videoCapture?.startRecording(
            file,
            Executors.newSingleThreadExecutor(),
            object : OnVideoSavedCallback {
                override fun onVideoSaved(@NonNull file: File) {
                    isRecordVideo = false
                    //保存视频成功回调，会在停止录制时被调用
                    ToastUtils.shortToast(" 录像成功 $file.absolutePath")
                }

                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    //保存失败的回调，可能在开始或结束录制时被调用
                    isRecordVideo = false
                    Log.e("", "onError: $message")
                    ToastUtils.shortToast(" 录像失败 $message")
                }
            })

        btnVideo.setOnClickListener {
            videoCapture?.stopRecording()//停止录制
            //preview?.clear()//清除预览
            btnVideo.text = "开始录像"
            btnVideo.setOnClickListener {
                btnVideo.text = "停止录像"
                takeVideo()
            }
            Log.d("path", file.path)
        }
    }

    /**
     * 开始相机预览
     */
    @SuppressLint("RestrictedApi")
    private fun startCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()//获取相机信息

            //预览配置
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCamera = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            videoCapture = VideoCapture.Builder()//录像用例配置
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) //设置高宽比
                .setTargetRotation(viewFinder?.display!!.rotation)//设置旋转角度
                .setAudioRecordSource(MediaRecorder.AudioSource.MIC)//设置音频源麦克风
                .build()
            try {
                cameraProvider?.unbindAll()//先解绑所有用例
                camera = cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCamera,
                    videoCapture
                )//绑定用例
            } catch (exc: Exception) {
                Log.e(TAG, "用例绑定失败", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == Constants.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ToastUtils.shortToast("请您打开必要权限")
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }
}