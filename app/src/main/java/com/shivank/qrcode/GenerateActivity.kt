package com.shivank.qrcode

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
class GenerateActivity : AppCompatActivity() {
    private lateinit var txtCodeData:TextView
    private lateinit var btnGenerate:Button
    private lateinit var imgQRCode:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate)
        txtCodeData=findViewById(R.id.txtCodeData)
        btnGenerate=findViewById(R.id.btnGenerate)
        imgQRCode=findViewById(R.id.imgQRCode)
        btnGenerate.setOnClickListener{
            val qrData= txtCodeData.toString()
            if(qrData.isEmpty()) {
                val bitmap = generateQRCode(txtCodeData.toString())
                imgQRCode.setImageBitmap(bitmap)
            }else{
                Toast.makeText(this,"Please enter a QR Code data!",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun generateQRCode(text: String): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        } catch (e: WriterException) { Log.d(TAG, "generateQRCode: ${e.message}") }
        return bitmap
    }
}