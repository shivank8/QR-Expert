package com.shivank.qrcode

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var btnScan :Button
    lateinit var txtResult: TextView
    private val Camera_Perm_code=122
    private val rStorage_Perm_Code=123
    private val wStorage_Perm_Code=123
    private lateinit var cameraLauncher:ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher:ActivityResultLauncher<Intent>

    lateinit var inputImage:InputImage
    lateinit var barcodeScanner: BarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnScan=findViewById(R.id.btnScan)
        txtResult=findViewById(R.id.txtResult)
        barcodeScanner=BarcodeScanning.getClient()
        cameraLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),object :ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult?) {
                    val data=result?.data

                    try{
                        val photo =data?.extras?.get("data") as Bitmap
                        inputImage= com.google.mlkit.vision.common.InputImage.fromBitmap(photo,0)
                        processQR()
                     }catch (e:Exception){

                    }
                }

            })
        galleryLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),object :ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult?) {
                    val data=result?.data
                    inputImage= com.google.mlkit.vision.common.InputImage.fromFilePath(this@MainActivity,data?.data)
                    processQR()
                }

            })

        btnScan.setOnClickListener {
            val option=arrayOf("camera","gallery")

            val builder=AlertDialog.Builder(this)
            builder.setTitle("Pick an Option")

            builder.setItems(option,DialogInterface.OnClickListener{dialog, which ->
                if(which==0){
                    val cameraIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraLauncher.launch(cameraIntent)
                }else{
                    val storage=Intent()
                    storage.setType("image/*")
                    storage.setAction(Intent.ACTION_GET_CONTENT)
                    galleryLauncher.launch(storage)

                }
            })
            builder.show()
        }
    }

    private fun processQR(){

     barcodeScanner.process(inputImage).addOnSuccessListener {
         for (barcode:Barcode in it){
             val valueType=barcode.valueType
             Toast.makeText(this, "valuetype $valueType", Toast.LENGTH_SHORT).show()
             when (valueType) {
                 Barcode.TYPE_WIFI -> {
                     val ssid = barcode.wifi!!.ssid
                     val password = barcode.wifi!!.password
                     val type = barcode.wifi!!.encryptionType

                     txtResult.text="ssid $ssid \n password $password type $type"
                 }
                 Barcode.TYPE_URL -> {
                     val title = barcode.url!!.title
                     val url = barcode.url!!.url

                     txtResult.text="title $title url $url "
                 }
                 Barcode.TYPE_TEXT->{
                     val data=barcode.displayValue
                     txtResult.text="result $data"
                 }
             }
         }
     }.addOnFailureListener{
         //vsva
     }
    }


    override fun onRestart() {
        super.onRestart()

        checkPermission(android.Manifest.permission.CAMERA,Camera_Perm_code)
    }

    private fun checkPermission(permission:String,requestCode:Int){
        if(ContextCompat.checkSelfPermission(this,permission)==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(permission),requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

         super.onRequestPermissionsResult(requestCode,permissions,grantResults)
        if(requestCode==Camera_Perm_code) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    rStorage_Perm_Code)
            }else{
                Toast.makeText(this, "Camera permission Denied", Toast.LENGTH_SHORT).show()
            }
        }else if(requestCode==rStorage_Perm_Code){
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    wStorage_Perm_Code)

            }else{
                Toast.makeText(this, "Storage permission Denied",
                    Toast.LENGTH_SHORT).show()
            }
        }else if(requestCode==wStorage_Perm_Code){
            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Storage permission Denied",
                    Toast.LENGTH_SHORT).show()


            }
        }


     }
}