package com.example.qrcodescannerapp // Apna package name likhein

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class ScanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Scan layout ko inflate karein
        val view = inflater.inflate(R.layout.fragment_scan, container, false)

        // Yahan aap apna camera initialize karne ka code likh sakte hain baad mein

        return view
    }
}