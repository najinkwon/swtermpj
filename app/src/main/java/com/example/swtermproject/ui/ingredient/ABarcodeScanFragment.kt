package com.example.swtermproject.ui.ingredient

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.swtermproject.R
import com.google.zxing.integration.android.IntentIntegrator

class ABarcodeScanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_barcode_scan,
            container,
            false
        )

        val btnStart = view.findViewById<Button>(R.id.btnStartBarcode)
        val btnResult = view.findViewById<Button>(R.id.btnBarcodeResult)

        btnStart.setOnClickListener {
            startBarcodeScanner()
        }

        btnResult.setOnClickListener {
            moveToResult("8801115111111")
        }

        return view
    }

    private fun startBarcodeScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this)

        integrator.setPrompt("바코드를 스캔하세요")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.initiateScan()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        val result = IntentIntegrator.parseActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(
                    requireContext(),
                    "스캔 취소",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "바코드: ${result.contents}",
                    Toast.LENGTH_SHORT
                ).show()

                moveToResult(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun moveToResult(barcode: String) {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                ABarcodeResultFragment.newInstance(barcode)
            )
            .commit()
    }
}