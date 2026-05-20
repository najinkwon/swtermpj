package com.example.swtermproject.ui.ingredient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.swtermproject.R

class AReceiptScanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_receipt_scan,
            container,
            false
        )

        val btnStart = view.findViewById<Button>(R.id.btnStartReceiptScan)
        val btnResult = view.findViewById<Button>(R.id.btnReceiptResult)

        btnStart.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "OCR 영수증 인식 연결 예정",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnResult.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    AReceiptResultFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}