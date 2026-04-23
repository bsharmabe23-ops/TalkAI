package com.example.talkai.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.example.talkai.assistant.GroqManager

class ObjectDetectionManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {

    private var lastAnnouncedTime = 0L
    private val announceInterval = 5000L
    private var isProcessing = false
    private var isReadingMode = false
    private var isNavigationMode = false
    private val groqManager = GroqManager()

    var onObjectDetected: ((String) -> Unit)? = null
    var onEmotionDetected: ((String) -> Unit)? = null

    // Set modes
    fun setReadingMode(reading: Boolean) {
        isReadingMode = reading
        isNavigationMode = false
    }

    fun setNavigationMode(navigating: Boolean) {
        isNavigationMode = navigating
        isReadingMode = false
    }

    // 📷 Back camera
    fun startCamera(previewView: PreviewView) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                val provider = future.get()
                provider.unbindAll()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .setOutputImageFormat(
                        ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
                    )
                    .build()

                analysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context)
                ) { imageProxy ->
                    processWithAI(imageProxy)
                }

                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // 🤳 Front camera
    fun startFrontCamera(previewView: PreviewView) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                val provider = future.get()
                provider.unbindAll()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val faceOptions = FaceDetectorOptions.Builder()
                    .setClassificationMode(
                        FaceDetectorOptions.CLASSIFICATION_MODE_ALL
                    )
                    .setMinFaceSize(0.15f)
                    .build()

                val faceDetector = FaceDetection.getClient(faceOptions)

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(
                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                    )
                    .build()

                analysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context)
                ) { imageProxy ->
                    processFaceFrame(imageProxy, faceDetector)
                }

                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // 🤖 Send to Groq Vision AI
    private fun processWithAI(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()

        if (now - lastAnnouncedTime < announceInterval
            || isProcessing) {
            imageProxy.close()
            return
        }

        try {
            val bitmap = imageProxyToBitmap(imageProxy)
            imageProxy.close()

            if (bitmap == null) {
                isProcessing = false
                return
            }

            isProcessing = true
            lastAnnouncedTime = now

            // Choose instruction based on mode
            val instruction = when {
                isReadingMode ->
                    "Read ALL the text visible in this image. " +
                            "If it is a book or document, read the sentences. " +
                            "If no text is visible say: " +
                            "'No text found, please hold closer to camera.' " +
                            "Just read the text, nothing else."

                isNavigationMode ->
                    "You are helping a blind person walk safely. " +
                            "Look at the center of this image. " +
                            "Is the walking path ahead clear or blocked? " +
                            "Reply in ONE sentence only. " +
                            "Examples: " +
                            "'Path is clear ahead.' " +
                            "'Warning: Chair blocking path ahead.' " +
                            "'Warning: Wall very close on left, move right.' " +
                            "Only mention what is directly in the walking path."

                else ->
                    "Describe the main objects you see. " +
                            "Format: 'Object on position.' " +
                            "Example: " +
                            "'Water bottle in center. Door on left. " +
                            "Chair on right.' " +
                            "One sentence only."
            }

            groqManager.describeImage(
                bitmap = bitmap,
                instruction = instruction,
                onResponse = { description ->
                    onObjectDetected?.invoke(description)
                    isProcessing = false
                },
                onError = {
                    onObjectDetected?.invoke(
                        if (isNavigationMode) "Path looks clear ahead"
                        else "Cannot describe scene"
                    )
                    isProcessing = false
                }
            )

        } catch (e: Exception) {
            imageProxy.close()
            isProcessing = false
        }
    }

    // 🖼️ Convert ImageProxy to Bitmap
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val plane = imageProxy.planes[0]
            val buffer = plane.buffer
            buffer.rewind()
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val bitmap = Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(
                java.nio.ByteBuffer.wrap(bytes)
            )

            val matrix = Matrix()
            matrix.postRotate(
                imageProxy.imageInfo.rotationDegrees.toFloat()
            )

            Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.width, bitmap.height,
                matrix, true
            )
        } catch (e: Exception) {
            null
        }
    }

    // 😊 Face emotion detection
    @androidx.annotation.OptIn(
        androidx.camera.core.ExperimentalGetImage::class
    )
    private fun processFaceFrame(
        imageProxy: ImageProxy,
        faceDetector: FaceDetector
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    val smile = face.smilingProbability ?: 0.5f
                    val leftEye = face.leftEyeOpenProbability ?: 1f
                    val rightEye = face.rightEyeOpenProbability ?: 1f
                    val eyesAvg = (leftEye + rightEye) / 2f

                    val emotion = when {
                        smile > 0.75f -> "very happy 😊"
                        smile > 0.5f -> "happy 🙂"
                        eyesAvg < 0.3f -> "sleepy 😴"
                        smile < 0.2f -> "sad 😢"
                        else -> "neutral 😐"
                    }
                    onEmotionDetected?.invoke(emotion)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}