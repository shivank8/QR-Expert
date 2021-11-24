package com.shivank.qrcode

import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.Barcode.*
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.shivank.qrcode.databinding.ActivityMainBinding
import java.util.concurrent.Executors

@ExperimentalGetImage
class ScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val cameraPermCode=12
    lateinit var camera: Camera
    private val TAG = "mytag"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission(android.Manifest.permission.CAMERA,cameraPermCode)

            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
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

        }, ContextCompat.getMainExecutor(this))

    }


    private fun processQRCodeImage(barcodeScanner: BarcodeScanner,
                                   imageProxy: ImageProxy
    ) {
        var url:String?=null
        var qrData:String?=null
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodeList ->
                    val barcode = barcodeList.getOrNull(0)
                    when (barcode?.valueType) {
                        TYPE_WIFI -> {
                            val ssid = barcode.wifi!!.ssid
                            val password = barcode.wifi!!.password
                            val type = barcode.wifi!!.encryptionType
                            url=""
                            qrData="SSID: $ssid \nPassword: $password \nType: $type "
                            binding.txtResult.text=qrData
                        }
                        TYPE_URL -> {
                            url = barcode.url!!.url
                            binding.txtResult.text="URL: $url"
                        }TYPE_CONTACT_INFO -> {
                        val name = barcode.contactInfo!!.name
                        val phone = barcode.contactInfo!!.phones
                        val email = barcode.contactInfo!!.emails
                        qrData="Name: $name \nPhone: $phone \nEmail: $email "
                        binding.txtResult.text=qrData
                    }TYPE_PHONE -> {
                        qrData = barcode.phone!!.number
                        binding.txtResult.text="Phone: $qrData "
                    }TYPE_EMAIL -> {
                        val add = barcode.email!!.address
                        val subj = barcode.email!!.subject
                        val body = barcode.email!!.body
                        qrData="Email : $add \nSubject: $subj \nBody: $body "
                        binding.txtResult.text=qrData
                    }TYPE_TEXT -> {
                        qrData = barcode.displayValue
                        binding.txtResult.text="BarCode Value: $qrData"
                    }TYPE_UNKNOWN -> {
                        qrData = barcode.displayValue
                        binding.txtResult.text="BarCode Value: $qrData"
                    }else -> {
                        val data = barcode?.displayValue
                        if (data != null) {
                            if(data.isNotEmpty()) {
                                barcode?.rawValue?.let { data ->
                                    qrData=data
                                    binding.txtResult.text ="BarCode Value: $qrData"

                                }
                            }
                        }
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
        binding.imgSearch.setOnClickListener{
            val browserIntent = Intent(Intent.ACTION_WEB_SEARCH)
            if(url.isNullOrEmpty()){
                browserIntent.putExtra(SearchManager.QUERY, qrData)
            }
            else{
                browserIntent.data = Uri.parse(url);
            }
            startActivity(browserIntent);
        }

    }


    override fun onRestart() {
        super.onRestart()
        checkPermission(android.Manifest.permission.CAMERA,cameraPermCode)
    }

    private fun checkPermission(permission:String,requestCode:Int){
        if(ContextCompat.checkSelfPermission(this,permission)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(permission),requestCode)
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
                Toast.makeText(this, "Camera permission Denied", Toast.LENGTH_SHORT).show()
            }
//        }else if(requestCode==rStorage_Perm_Code){
//            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    wStorage_Perm_Code)
//
//            }else{
//                Toast.makeText(this, "Storage permission Denied",
//                    Toast.LENGTH_SHORT).show()
//            }
//        }else if(requestCode==wStorage_Perm_Code){
//            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                Toast.makeText(this, "Storage permission Denied",
//                    Toast.LENGTH_SHORT).show()
//
//
//            }
        }


    }
}