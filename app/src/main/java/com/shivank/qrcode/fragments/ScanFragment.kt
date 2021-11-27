package com.shivank.qrcode.fragments

import android.app.Activity
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.mlkit.vision.barcode.Barcode.*
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.shivank.qrcode.R
import com.shivank.qrcode.databinding.ActivityMainBinding
import java.util.concurrent.Executors

@ExperimentalGetImage
class ScanFragment : Fragment() {
    private lateinit var binding: ActivityMainBinding
    private val cameraPermCode=12
    lateinit var camera: Camera
    private val TAG = "mytag"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view=(binding.root)
        checkPermission(android.Manifest.permission.CAMERA,cameraPermCode)

            val cameraProviderFuture = context?.let { ProcessCameraProvider.getInstance(it) }

        cameraProviderFuture?.addListener({
            val cameraProvider = cameraProviderFuture?.get()
            val previewUseCase = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.prvCam.surfaceProvider)
            }

            val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
                FORMAT_CODE_128,
                FORMAT_CODE_39,
                FORMAT_CODE_93,
                FORMAT_EAN_8,
                FORMAT_EAN_13,
                FORMAT_QR_CODE,
                FORMAT_UPC_A,
                FORMAT_UPC_E,
                FORMAT_PDF417
            ).build()

            val scanner = BarcodeScanning.getClient(options)
            val analysisUseCase = ImageAnalysis.Builder().build()

            analysisUseCase.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                { imageProxy -> processQRCodeImage(scanner, imageProxy) }
            )
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                camera =cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    previewUseCase,
                    analysisUseCase)


            }catch (e:Exception) {
                Log.e(TAG, e.message!!)
            }
            // toggle flash
            if (camera.cameraInfo.hasFlashUnit()) {
                binding.imgFlash.visibility = View.VISIBLE
                binding.imgFlash.setOnClickListener {
                    if( camera.cameraInfo.torchState.value == TorchState.OFF) {
                        camera.cameraControl.enableTorch(true)
                        binding.imgFlash.setImageResource(R.drawable.ic_flash_on)
                    } else {
                        camera.cameraControl.enableTorch(false)
                        binding.imgFlash.setImageResource(R.drawable.ic_flash_off)
                    }
                }
            } else {
                binding.imgFlash.visibility = View.GONE
            }

        }, context?.let { ContextCompat.getMainExecutor(it) })
    return view
    }


    private fun processQRCodeImage(barcodeScanner: BarcodeScanner,
                                   imageProxy: ImageProxy
    ) {
        var qrData=""
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodeList ->
                    val barcode = barcodeList.getOrNull(0)
                    if(barcode?.valueType.toString()!="null" && barcode?.valueType.toString()!="n" ) {
                        when (barcode?.valueType) {
                            TYPE_WIFI -> {
                                val ssid = barcode.wifi!!.ssid
                                val password = barcode.wifi!!.password
                                val type = barcode.wifi!!.encryptionType
                                qrData = "SSID: $ssid \nPassword: $password \nType: $type "
                                binding.txtResult.text = qrData
                            }
                            TYPE_URL -> {
                                qrData = barcode.url!!.url
                                binding.txtResult.text = "URL: $qrData"
                            }
                            TYPE_PHONE -> {
                                qrData = barcode.phone!!.number
                                binding.txtResult.text = "Phone: $qrData "
                            }
                            TYPE_EMAIL -> {
                                val add = barcode.email!!.address
                                val subj = barcode.email!!.subject
                                val body = barcode.email!!.body
                                qrData = "Email : $add \nSubject: $subj \nBody: $body "
                                binding.txtResult.text = qrData
                            }
                            TYPE_TEXT -> {
                                qrData = barcode.displayValue
                                binding.txtResult.text = "BarCode Value: $qrData"
                            }
                            TYPE_UNKNOWN -> {
                                if (barcode.displayValue.isNotEmpty())
                                    qrData = barcode.displayValue
                                binding.txtResult.text = "BarCode Value: $qrData"
                            }
                            else -> {
                                val data = barcode?.displayValue
                                if (data != null) {
                                    if (data.isNotEmpty()) {
                                        barcode.rawValue?.let { data ->
                                            if (data.isNotEmpty())
                                                qrData = data
                                            binding.txtResult.text = "BarCode Value: $qrData"

                                        }

                                    }
                                }
                            }
                        }

                        binding.imgSearch.setOnClickListener {

                            when {
                                qrData.startsWith("http") -> {
                                    Toast.makeText(
                                        context,
                                        "Opening URL in browser",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val  intent =  Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse(qrData);
                                    startActivity(intent);
                                }
                                qrData.isNotEmpty() -> {
                                    val browserIntent = Intent(Intent.ACTION_WEB_SEARCH)
                                    browserIntent.putExtra(SearchManager.QUERY, qrData)
                                    startActivity(browserIntent);
                                }
                                else -> {
                                    Toast.makeText(
                                        context,
                                        "No scanned data found!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        binding.imgCopyText.setOnClickListener {
                            val clipboard: ClipboardManager = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("QR value",binding.txtResult.text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context,"QR code value copied to clipboard." , Toast.LENGTH_SHORT).show()

                        }
                    }

                }
                .addOnFailureListener {
                    Log.e(TAG, it.message!!)
                }.addOnCompleteListener {
                    imageProxy.image?.close()
                    imageProxy.close()
                }
        }


    }

    private fun checkPermission(permission:String,requestCode:Int){
        if(context?.let { ContextCompat.checkSelfPermission(it,permission) } == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(context as Activity, arrayOf(permission),requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        if(requestCode==cameraPermCode) {

            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(context, "Camera permission Denied", Toast.LENGTH_SHORT).show()
            }
        }


    }
}