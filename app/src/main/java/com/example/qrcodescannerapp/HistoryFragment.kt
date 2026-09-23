package com.example.qrcodescannerapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // History layout ko inflate karein
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        // RecyclerView aur filters ka logical code yahan handle hoga

        return view
    }
}