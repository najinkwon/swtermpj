package com.example.swtermproject.ui.ingredient

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.swtermproject.R
import com.example.swtermproject.ocr.BReceiptItemCandidate
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

    private val cameraPreview =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap == null) {
                Toast.makeText(
                    requireContext(),
                    "촬영이 취소되었습니다",
                    Toast.LENGTH_SHORT
                ).show()
                return@registerForActivityResult
            }

            recognizeReceiptBitmap(bitmap)
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                cameraPreview.launch(null)
            } else {
                Toast.makeText(
                    requireContext(),
                    "카메라 권한이 필요합니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
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

        btnStart.backgroundTintList = null
        btnStart.setBackgroundResource(R.drawable.bg_primary_button)
        btnStart.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.white)
        )

        btnResult.backgroundTintList = null
        btnResult.setBackgroundResource(R.drawable.bg_chip_white)
        btnResult.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.primary_green_dark)
        )

        btnStart.setOnClickListener {
            showScanOptionDialog()
        }

        // 테스트용 버튼
        btnResult.setOnClickListener {
            moveToResult(
                listOf(
                    BReceiptItemCandidate(
                        name = "우유",
                        amountText = "1L",
                        amount = 1000.0,
                        unit = "ml",
                        amountGram = null,
                        category = "유제품",
                        amountSource = "test"
                    ),
                    BReceiptItemCandidate(
                        name = "계란",
                        amountText = "10입",
                        amount = 10.0,
                        unit = "개",
                        amountGram = null,
                        category = "단백질",
                        amountSource = "test"
                    ),
                    BReceiptItemCandidate(
                        name = "닭가슴살",
                        amountText = "300g",
                        amount = 300.0,
                        unit = "g",
                        amountGram = 300.0,
                        category = "단백질",
                        amountSource = "test"
                    )
                )
            )
        }

        return view
    }

    private fun showScanOptionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("영수증 인식")
            .setItems(
                arrayOf(
                    "카메라로 촬영",
                    "갤러리에서 선택"
                )
            ) { _, which ->
                when (which) {
                    0 -> startCamera()
                    1 -> imagePicker.launch("image/*")
                }
            }
            .show()
    }

    private fun startCamera() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            cameraPreview.launch(null)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun recognizeReceiptImage(uri: Uri) {
        setLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ocrManager.recognizeTextFromImage(uri)
            handleOcrResult(result)
        }
    }

    private fun recognizeReceiptBitmap(bitmap: Bitmap) {
        setLoading(true)

        viewLifecycleOwner.lifecycleScope.launch {
            val result = ocrManager.recognizeTextFromBitmap(bitmap)
            handleOcrResult(result)
        }
    }

    private fun handleOcrResult(result: Result<String>) {
        result.onSuccess { rawText ->
            val candidates = ocrManager.extractItemsFromText(rawText)
                .filter { it.name.isNotBlank() }
                .distinctBy { it.name }
                .take(12)

            if (candidates.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "영수증에서 재료 후보를 찾지 못했어요",
                    Toast.LENGTH_SHORT
                ).show()

                setLoading(false)
            } else {
                setLoading(false)
                moveToResult(candidates)
            }
        }.onFailure {
            Toast.makeText(
                requireContext(),
                it.message ?: "OCR 인식에 실패했습니다",
                Toast.LENGTH_SHORT
            ).show()

            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        btnStart.isEnabled = !isLoading
        btnResult.isEnabled = !isLoading

        btnStart.text =
            if (isLoading) {
                "영수증 인식 중..."
            } else {
                "영수증 인식 시작"
            }
    }

    private fun moveToResult(candidates: List<BReceiptItemCandidate>) {
        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragmentContainer,
                AReceiptResultFragment.newInstanceFromCandidates(candidates)
            )
            .addToBackStack(null)
            .commit()
    }
}