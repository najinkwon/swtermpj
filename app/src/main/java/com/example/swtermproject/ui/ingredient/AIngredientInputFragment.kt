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
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.model.ATempIngredientStore
import com.example.swtermproject.data.model.Ingredient

class AIngredientInputFragment : Fragment() {

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
        val textCategory = view.findViewById<TextView>(R.id.textCategory)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnReceiptScan = view.findViewById<Button>(R.id.btnReceiptScan)
        val btnBarcodeScan = view.findViewById<Button>(R.id.btnBarcodeScan)

        btnSave.setOnClickListener {
            val name = editName.text.toString()
            val initialAmount = editInitialAmount.text.toString().toIntOrNull()
            val currentAmount = editCurrentAmount.text.toString().toIntOrNull()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "재료명을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (initialAmount == null || currentAmount == null || initialAmount <= 0) {
                Toast.makeText(requireContext(), "수량을 올바르게 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = when (name) {
                "간장" -> "조미료/소스"
                "우유" -> "유제품"
                "대파" -> "채소"
                "계란" -> "단백질"
                "양파" -> "채소"
                "두부" -> "단백질"
                else -> "기타"
            }

            val percent = ((currentAmount.toDouble() / initialAmount.toDouble()) * 100).toInt()

            textCategory.text = category

            val isNewItem = ATempIngredientStore.addIngredient(
                Ingredient(
                    name = name,
                    category = category,
                    percent = percent
                )
            )

            val message = if (isNewItem) {
                "$name 저장 완료!"
            } else {
                "$name 수량 갱신 완료!"
            }

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            (activity as MainActivity).openIngredientList()
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
}