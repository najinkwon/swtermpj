package com.example.swtermproject.ui.ingredient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.viewmodel.BIngredientViewModel
import kotlinx.coroutines.launch

class AIngredientInputFragment : Fragment() {

    private val viewModel: BIngredientViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_ingredient_input,
            container,
            false
        )

        val editName = view.findViewById<EditText>(R.id.editIngredientName)
        val editInitialAmount = view.findViewById<EditText>(R.id.editInitialAmount)
        val editCurrentAmount = view.findViewById<EditText>(R.id.editCurrentAmount)
        val editExpiryDate = view.findViewById<EditText>(R.id.editExpiryDate)
        val textCategory = view.findViewById<TextView>(R.id.textCategory)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnReceiptScan = view.findViewById<Button>(R.id.btnReceiptScan)
        val btnBarcodeScan = view.findViewById<Button>(R.id.btnBarcodeScan)

        textCategory.text = "저장 시 자동 분류됩니다"

        btnSave.setOnClickListener {
            val name = editName.text.toString().trim()
            val initialAmount = editInitialAmount.text.toString().toDoubleOrNull()
            val currentAmount = editCurrentAmount.text.toString().toDoubleOrNull()
            val expiryDate = editExpiryDate.text.toString().trim()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "재료명을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (initialAmount == null || currentAmount == null || initialAmount <= 0.0) {
                Toast.makeText(requireContext(), "수량을 올바르게 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentAmount < 0.0) {
                Toast.makeText(requireContext(), "현재 남은 양은 0 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentAmount > initialAmount) {
                Toast.makeText(requireContext(), "현재 남은 양은 구매량보다 클 수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (expiryDate.isNotBlank() && !expiryDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                Toast.makeText(requireContext(), "유통기한은 2026-06-30 형식으로 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            textCategory.text = "분류 중..."

            viewModel.addIngredient(
                name = name,
                initialAmount = initialAmount,
                currentAmount = currentAmount,
                unit = "개",
                expiryDate = expiryDate,
                storageType = "냉장"
            )
        }

        btnReceiptScan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AReceiptScanFragment())
                .commit()
        }

        btnBarcodeScan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ABarcodeScanFragment())
                .commit()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeMessage()
    }

    private fun observeMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.message.collect { message ->
                    if (message == null) return@collect

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    val shouldMoveToList =
                        message.contains("추가") || message.contains("저장")

                    viewModel.clearMessage()

                    if (shouldMoveToList) {
                        (activity as MainActivity).openIngredientList()
                    }
                }
            }
        }
    }
}
