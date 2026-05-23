package com.example.swtermproject.ui.ingredient

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.swtermproject.R
import com.example.swtermproject.ocr.BReceiptOcrManager
import kotlinx.coroutines.launch

class AReceiptScanFragment : Fragment() {

    private lateinit var ocrManager: BReceiptOcrManager
    private lateinit var btnStart: Button
    private lateinit var btnResult: Button

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) {
                Toast.makeText(
                    requireContext(),
                    "이미지 선택이 취소되었습니다",
                    Toast.LENGTH_SHORT
                ).show()
                return@registerForActivityResult
            }

            recognizeReceiptImage(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ocrManager = BReceiptOcrManager(requireContext().applicationContext)
    }

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

        btnStart = view.findViewById(R.id.btnStartReceiptScan)
        btnResult = view.findViewById(R.id.btnReceiptResult)

        btnStart.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnResult.setOnClickListener {
            moveToResult(
                listOf("우유", "계란", "양파")
            )
        }

        return view
    }

    private fun recognizeReceiptImage(uri: Uri) {
        btnStart.isEnabled = false
        btnResult.isEnabled = false

        Toast.makeText(
            requireContext(),
            "영수증을 인식하고 있어요",
            Toast.LENGTH_SHORT
        ).show()

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ocrManager.recognizeTextFromImage(uri)

            result.onSuccess { rawText ->
                val candidates = ocrManager.extractItemsFromText(rawText)
                val names = candidates
                    .map { it.name }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(12)

                if (names.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "영수증에서 재료 후보를 찾지 못했어요",
                        Toast.LENGTH_SHORT
                    ).show()

                    btnStart.isEnabled = true
                    btnResult.isEnabled = true
                } else {
                    moveToResult(names)
                }
            }.onFailure {
                Toast.makeText(
                    requireContext(),
                    it.message ?: "OCR 인식에 실패했습니다",
                    Toast.LENGTH_SHORT
                ).show()

                btnStart.isEnabled = true
                btnResult.isEnabled = true
            }
        }
    }

    private fun moveToResult(names: List<String>) {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                AReceiptResultFragment.newInstance(names)
            )
            .addToBackStack(null)
            .commit()
    }
}
