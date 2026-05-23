package com.example.swtermproject.ui.ingredient

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.swtermproject.R
import com.example.swtermproject.domain.model.BIngredient
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AIngredientDetailBottomSheet(
    private val ingredient: BIngredient,
    private val expireDay: Int,
    private val onAmountChanged: (Double) -> Unit,
    private val onIngredientUpdated: (BIngredient) -> Unit,
    private val onChanged: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var progress: ProgressBar
    private lateinit var progressExpire: ProgressBar
    private lateinit var textPercent: TextView
    private lateinit var textExpireDetail: TextView
    private lateinit var textName: TextView
    private lateinit var textCategory: TextView
    private lateinit var textEmoji: TextView

    private var currentIngredient: BIngredient = ingredient
    private var currentPercent = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_ingredient_detail_bottom,
            container,
            false
        )

        currentPercent = currentIngredient.stockPercent.coerceIn(0, 100)

        textEmoji = view.findViewById(R.id.textDetailEmoji)
        textName = view.findViewById(R.id.textDetailName)
        textCategory = view.findViewById(R.id.textDetailCategory)

        progress = view.findViewById(R.id.progressIngredient)
        progressExpire = view.findViewById(R.id.progressExpire)
        textPercent = view.findViewById(R.id.textDetailPercent)
        textExpireDetail = view.findViewById(R.id.textExpireDetail)

        val btnUse10 = view.findViewById<Button>(R.id.btnUse10)
        val btnUse30 = view.findViewById<Button>(R.id.btnUse30)
        val btnRefill = view.findViewById<Button>(R.id.btnRefill)
        val btnEdit = view.findViewById<Button>(R.id.btnEditIngredient)
        val btnClose = view.findViewById<Button>(R.id.btnCloseBottom)

        progressExpire.progress = expireDay.coerceIn(0, 30)
        textExpireDetail.text = "D-$expireDay"

        bindIngredientInfo()
        updateUI(false)

        btnUse10.setOnClickListener {
            decreasePercent(10)
        }

        btnUse30.setOnClickListener {
            decreasePercent(30)
        }

        btnRefill.setOnClickListener {
            currentPercent = 100
            persistCurrentPercent()
            updateUI(false)
            onChanged()

            Toast.makeText(
                requireContext(),
                "재료를 리필했어요",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnEdit.setOnClickListener {
            showEditDialog()
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun bindIngredientInfo() {
        textName.text = currentIngredient.name
        textCategory.text = currentIngredient.category

        textEmoji.text = when (currentIngredient.category) {
            "채소" -> "🥬"
            "유제품" -> "🥛"
            "단백질" -> "🥚"
            "조미료/소스" -> "🥫"
            else -> "🍽️"
        }
    }

    private fun showEditDialog() {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }

        val editName = EditText(context).apply {
            hint = "재료명"
            setText(currentIngredient.name)
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val categorySpinner = Spinner(context)
        val categories = listOf("채소", "유제품", "단백질", "조미료/소스", "기타")
        categorySpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        categorySpinner.setSelection(
            categories.indexOf(currentIngredient.category).takeIf { it >= 0 } ?: categories.lastIndex
        )

        val editInitialAmount = EditText(context).apply {
            hint = "구매량"
            setText(formatAmount(currentIngredient.initialAmount))
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val editCurrentAmount = EditText(context).apply {
            hint = "현재 남은 양"
            setText(formatAmount(currentIngredient.currentAmount))
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val editExpiryDate = EditText(context).apply {
            hint = "유통기한 예: 2026-06-30"
            setText(currentIngredient.expiryDate)
            inputType = InputType.TYPE_CLASS_DATETIME
        }

        val editUnit = EditText(context).apply {
            hint = "단위 예: 개, ml, g"
            setText(currentIngredient.unit)
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val storageSpinner = Spinner(context)
        val storageTypes = listOf("냉장", "냉동", "실온")
        storageSpinner.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            storageTypes
        )
        storageSpinner.setSelection(
            storageTypes.indexOf(currentIngredient.storageType).takeIf { it >= 0 } ?: 0
        )

        container.addView(makeLabel("재료명"))
        container.addView(editName)
        container.addView(makeLabel("카테고리"))
        container.addView(categorySpinner)
        container.addView(makeLabel("구매량"))
        container.addView(editInitialAmount)
        container.addView(makeLabel("현재 남은 양"))
        container.addView(editCurrentAmount)
        container.addView(makeLabel("유통기한"))
        container.addView(editExpiryDate)
        container.addView(makeLabel("단위"))
        container.addView(editUnit)
        container.addView(makeLabel("보관 방식"))
        container.addView(storageSpinner)

        AlertDialog.Builder(context)
            .setTitle("재료 정보 수정")
            .setView(container)
            .setPositiveButton("저장", null)
            .setNegativeButton("취소", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = editName.text.toString().trim()
                        val initialAmount = editInitialAmount.text.toString().toDoubleOrNull()
                        val currentAmount = editCurrentAmount.text.toString().toDoubleOrNull()
                        val expiryDate = editExpiryDate.text.toString().trim()
                        val unit = editUnit.text.toString().trim().ifBlank { "개" }

                        if (name.isBlank()) {
                            Toast.makeText(context, "재료명을 입력하세요", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (initialAmount == null || initialAmount <= 0.0) {
                            Toast.makeText(context, "구매량을 올바르게 입력하세요", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (currentAmount == null || currentAmount < 0.0) {
                            Toast.makeText(context, "현재 남은 양을 올바르게 입력하세요", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (currentAmount > initialAmount) {
                            Toast.makeText(context, "현재 남은 양은 구매량보다 클 수 없습니다", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (expiryDate.isNotBlank() && !expiryDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                            Toast.makeText(context, "유통기한은 2026-06-30 형식으로 입력하세요", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val updated = currentIngredient.copy(
                            name = name,
                            category = categorySpinner.selectedItem.toString(),
                            initialAmount = initialAmount,
                            currentAmount = currentAmount,
                            unit = unit,
                            expiryDate = expiryDate,
                            storageType = storageSpinner.selectedItem.toString()
                        )

                        currentIngredient = updated
                        currentPercent = updated.stockPercent.coerceIn(0, 100)

                        onIngredientUpdated(updated)
                        bindIngredientInfo()
                        updateUI(false)
                        onChanged()

                        Toast.makeText(context, "재료 정보를 수정했어요", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                }

                show()
            }
    }

    private fun makeLabel(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setPadding(0, 18, 0, 4)
        }
    }

    private fun decreasePercent(amount: Int) {
        currentPercent -= amount

        if (currentPercent < 0) {
            currentPercent = 0
        }

        persistCurrentPercent()
        updateUI(true)
        onChanged()
    }

    private fun persistCurrentPercent() {
        val newCurrentAmount =
            currentIngredient.initialAmount * (currentPercent.toDouble() / 100.0)

        currentIngredient = currentIngredient.copy(
            currentAmount = newCurrentAmount
        )

        onAmountChanged(newCurrentAmount)
    }

    private fun updateUI(showToast: Boolean) {
        progress.progress = currentPercent
        textPercent.text = "$currentPercent%"

        val color =
            if (currentPercent <= 20) {
                Color.parseColor("#FF5F7E")
            } else {
                Color.parseColor("#4CAF50")
            }

        textPercent.setTextColor(color)

        if (showToast && currentPercent <= 20) {
            Toast.makeText(
                requireContext(),
                "재료가 부족해요!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun formatAmount(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }
}
