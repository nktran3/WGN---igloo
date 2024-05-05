package com.example.wgn_igloo.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.wgn_igloo.R
import com.example.wgn_igloo.databinding.BarcodeScanningBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val API_KEY = "LLIyBbVQ7WJgTsWITh4TwWNHDBojLnJG2ypcXWAg"
private const val TAG = "BarcodeScanner"


class BarcodeScannerFragment : Fragment() {

    private var _binding: BarcodeScanningBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BarcodeScanningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!allPermissionsGranted()) {
            requestPermissions()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.scanButton.setOnClickListener {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                requestPermissions()
            }
        }
    }


    // ChatGpt assisted function -> used to help establish the skeleton of using the CameraX library
    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        Log.d(TAG, "Camera service launched")
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            Log.d(TAG, "Listener attached to camera")

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            Log.d(TAG, "Preview launched")

            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
                            // ML Kit's Barcode Scanner
                            val scanner = BarcodeScanning.getClient()
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    if (barcodes.isNotEmpty()) {
                                        val firstBarcode = barcodes[0]
                                        val upc = firstBarcode.rawValue
                                        Log.d(TAG, "Barcode: $upc")
                                        if (upc != null) {
                                            upcLookup(upc)
                                            cameraExecutor.shutdown()
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Handle error
                                    Log.e(TAG, "Barcode scanning failed", e)
                                }
                                .addOnCompleteListener {
                                    // It's important to close the imageProxy
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close() // Close the imageProxy if no image is found
                        }
                    })
                }

            // Bind use cases to the camera lifecycle
            try {
                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    private fun upcLookup(upc: String) {

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.nal.usda.gov/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val foodDatabaseApi: USDAFoodDatabaseAPI =
            retrofit.create<USDAFoodDatabaseAPI>(USDAFoodDatabaseAPI::class.java)
        lifecycleScope.launch {
            try {
                val response = foodDatabaseApi.fetchFoodInfoByUPC(API_KEY, upc, "Branded")
                if (response.foods.isNotEmpty()) {
                    // Safely access the first item in the list
                    val foodFDCId = response.foods[0].fdcId
                    val foodDetailResponse =
                        foodDatabaseApi.fetchFoodInfoByFDCID(foodFDCId, API_KEY,)
                    val foodDescription = foodDetailResponse.description
                    val foodBrand = foodDetailResponse.brandOwner
                    binding.textView.text = "$foodBrand $foodDescription"
                    val testMessage = "$foodDescription"
                    val formFragment = NewItemsFormFragment.newInstance(testMessage)
                    requireActivity().supportFragmentManager.beginTransaction().replace(R.id.fragment_container, formFragment).commit()
                    Toast.makeText(requireContext(), "Adding manually", Toast.LENGTH_SHORT).show()
                    true


                    Log.d(TAG, "$foodDescription")
                } else {
                    // Handle the case where no foods were returned
                    Log.d(TAG, "No foods found for the given UPC.")
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to fetch: $ex")
            }
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}