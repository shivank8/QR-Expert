package com.shivank.qrcode.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.shivank.qrcode.R

class DashboardFragment : Fragment() {
    private lateinit var btnScan :Button
    private lateinit var btnGen :Button
    @SuppressLint("UnsafeOptInUsageError")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_dashboard, container, false)
        btnScan =view.findViewById(R.id.btnQR)
        btnGen = view.findViewById(R.id.btnGen)
        btnScan.setOnClickListener{
            changeFragment(ScanFragment())
        }
        btnGen.setOnClickListener{
            changeFragment(GenerateFragment())
       }
        return view
    }
    private fun changeFragment(fragment: Fragment) {
        val fragmentManager = activity?.supportFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        fragmentTransaction?.replace(R.id.fragmentContainer,fragment)
        fragmentTransaction?.addToBackStack(null)
        fragmentTransaction?.commit()
    }

}