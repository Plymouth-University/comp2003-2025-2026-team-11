package com.example.comp2003_prototype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.comp2003_prototype.R

class AiFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // This links the Kotlin file to the XML layout we just made
        return inflater.inflate(R.layout.fragment_ai, container, false)
    }
}