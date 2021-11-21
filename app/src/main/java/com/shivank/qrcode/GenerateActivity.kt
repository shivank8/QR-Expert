package com.shivank.qrcode

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
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
            val qrData= txtCodeData.text.toString()
            if(qrData.isNotEmpty()) {
                closeSoftKeyboard(this,txtCodeData)
                generateQRCode(qrData)

            }else{
                Toast.makeText(this,getString(R.string.enter_qr_data),Toast.LENGTH_SHORT).show()
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
            imgQRCode.setImageBitmap(bitmap)
            Toast.makeText(this,getString(R.string.qr_generated),Toast.LENGTH_SHORT).show()

        } catch (e: WriterException) { Log.d(TAG, e.message.toString()) }
        return bitmap
    }
    private fun closeSoftKeyboard(context: Context, v: View) {
        val iMm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        iMm.hideSoftInputFromWindow(v.windowToken, 0)
        v.clearFocus()
    }
}