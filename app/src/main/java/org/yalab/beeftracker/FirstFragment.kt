package org.yalab.beeftracker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import kotlinx.android.synthetic.main.fragment_first.*
import kotlinx.coroutines.*
import org.yalab.beeftracker.databinding.FragmentFirstBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    companion object {
        private const val TAG = "CameraXBasic"
    }
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var foodLabel: FoodLabel? = null
    private var _binding: FragmentFirstBinding? = null
    var step = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCamera()
    }

    fun renderCattleInfo(beefTrackingNumber: String) = runBlocking {
        val context = context as Context
        launch {
            val nlbc = NLBC()
            val deferred = async(Dispatchers.IO) {
                nlbc.fetch(beefTrackingNumber)
            }
            deferred.await()
            val cattle = nlbc.cattle
            binding.cattleInfo.trackingNumber.text = cattle.trackingNumber
            binding.cattleInfo.birthDay.text = cattle.birthDay
            binding.cattleInfo.gender.text = cattle.gender
            binding.cattleInfo.motherTrackingNumber.text = cattle.motherTrackingNumber
            binding.cattleInfo.breed.text = cattle.breed
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        imageCapture = ImageCapture.Builder()
            .build()
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(Surface.ROTATION_270)
            .build()
        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(viewFinder.surfaceProvider) }
        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        cameraProviderFuture.get().bindToLifecycle(this as LifecycleOwner, cameraSelector, imageAnalysis, preview)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            imageAnalysis!!.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), ImageAnalysis.Analyzer { image ->
                if(image.format == ImageFormat.YUV_420_888) {
                    if(foodLabel == null) {
                        foodLabel = FoodLabel(requireContext(), image)
                    }
                    foodLabel!!.nextFrame(image)
                    val number = foodLabel!!.beefTrackingNumber()
                    try {
                        binding.imageView.setImageBitmap(foodLabel!!.bitmap)
                        if (number.length > 9) {
                            renderCattleInfo(number)
                            cameraProvider.unbindAll()
                        }
                    }catch(e: Exception) {
                        println(e)
                    }
                }
            })
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalysis, preview)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }
}
