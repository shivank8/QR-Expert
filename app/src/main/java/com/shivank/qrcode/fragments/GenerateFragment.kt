package com.shivank.qrcode.fragments

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.shivank.qrcode.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class GenerateFragment : Fragment() {
    private lateinit var txtCodeData:TextView
    private lateinit var btnGenerate:Button
    private lateinit var btnSave:Button
    private var bitmap: Bitmap? = null
    private val storagePermCode=15
    private lateinit var imgQRCode:ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_generate, container, false)

        checkPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,storagePermCode)
        txtCodeData=view.findViewById(R.id.txtCodeData)
        btnGenerate=view.findViewById(R.id.btnGenerate)
        btnSave=view.findViewById(R.id.btnSave)
        imgQRCode=view.findViewById(R.id.imgQRCode)
        btnGenerate.setOnClickListener{
            val qrData= txtCodeData.text.toString()
            if(qrData.isNotEmpty()) {
                context?.let { it1 -> closeSoftKeyboard(it1,txtCodeData) }
                bitmap= generateQRCode(qrData)

            }else{
                Toast.makeText(context,getString(R.string.enter_qr_data),Toast.LENGTH_SHORT).show()
            }
        }
        btnSave.setOnClickListener{
            val root = Environment.getExternalStorageDirectory().absoluteFile
            val myDir = File("$root/Saved QR Code")
            myDir.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fname = "QR Code $timeStamp.jpg"
            val file = File(myDir, fname)
            try {
                val out = FileOutputStream(file)
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
                out.close()
                Toast.makeText(context, R.string.qr_saved,Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context,"Unable to save QR Code!",Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
        return view
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
            //Toast.makeText(this,getString(R.string.qr_generated),Toast.LENGTH_SHORT).show()
        } catch (e: WriterException) { Log.d(TAG, e.message.toString()) }
        return bitmap
    }

    private fun checkPermission(permission:String,requestCode:Int){
        if(ContextCompat.checkSelfPermission(requireContext(),permission)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission),requestCode)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == storagePermCode) {

            if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(context, "Storage permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun closeSoftKeyboard(context: Context, v: View) {
        val iMm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        iMm.hideSoftInputFromWindow(v.windowToken, 0)
        v.clearFocus()
    }
}