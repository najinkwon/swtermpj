package com.example.swtermproject.ui.ingredient

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.swtermproject.R
import com.example.swtermproject.data.model.ATempIngredientStore
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AIngredientDetailBottomSheet(
    private val ingredientName: String,
    private val onChanged: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var progress: ProgressBar
    private lateinit var progressExpire: ProgressBar

    private lateinit var textPercent: TextView
    private lateinit var textExpireDetail: TextView

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

        val ingredient =
            ATempIngredientStore.ingredients.find {
                it.name == ingredientName
            }

        if (ingredient == null) {
            dismiss()
            return view
        }

        currentPercent = ingredient.percent

        val textEmoji =
            view.findViewById<TextView>(R.id.textDetailEmoji)

        val textName =
            view.findViewById<TextView>(R.id.textDetailName)

        val textCategory =
            view.findViewById<TextView>(R.id.textDetailCategory)

        progress =
            view.findViewById(R.id.progressIngredient)

        progressExpire =
            view.findViewById(R.id.progressExpire)

        textPercent =
            view.findViewById(R.id.textDetailPercent)

        textExpireDetail =
            view.findViewById(R.id.textExpireDetail)

        val btnUse10 =
            view.findViewById<Button>(R.id.btnUse10)

        val btnUse30 =
            view.findViewById<Button>(R.id.btnUse30)

        val btnRefill =
            view.findViewById<Button>(R.id.btnRefill)

        val btnClose =
            view.findViewById<Button>(R.id.btnCloseBottom)

        textName.text = ingredient.name

        textCategory.text = ingredient.category

        textEmoji.text = when (ingredient.category) {
            "채소" -> "🥬"
            "유제품" -> "🥛"
            "단백질" -> "🥚"
            "조미료/소스" -> "🥫"
            else -> "🍽️"
        }

        progressExpire.progress =
            ingredient.expireDay

        textExpireDetail.text =
            "D-${ingredient.expireDay}"

        updateUI(false)

        btnUse10.setOnClickListener {

            currentPercent -= 10

            if (currentPercent < 0) {
                currentPercent = 0
            }

            ingredient.percent = currentPercent

            updateUI(true)

            onChanged()
        }

        btnUse30.setOnClickListener {

            currentPercent -= 30

            if (currentPercent < 0) {
                currentPercent = 0
            }

            ingredient.percent = currentPercent

            updateUI(true)

            onChanged()
        }

        btnRefill.setOnClickListener {

            currentPercent = 100

            ingredient.percent = currentPercent

            updateUI(false)

            onChanged()

            Toast.makeText(
                requireContext(),
                "재료를 리필했어요",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnClose.setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun updateUI(
        showToast: Boolean
    ) {

        progress.progress = currentPercent

        textPercent.text =
            "$currentPercent%"

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
}