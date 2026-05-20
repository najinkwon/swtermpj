package com.example.swtermproject.ui.ingredient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.model.ATempIngredientStore
import com.example.swtermproject.data.model.Ingredient

class AReceiptResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_receipt_result,
            container,
            false
        )

        val btnAdd = view.findViewById<Button>(R.id.btnAddReceiptItems)

        btnAdd.setOnClickListener {
            ATempIngredientStore.addIngredient(
                Ingredient(
                    name = "우유",
                    category = "유제품",
                    percent = 100
                )
            )

            ATempIngredientStore.addIngredient(
                Ingredient(
                    name = "계란",
                    category = "단백질",
                    percent = 100
                )
            )

            ATempIngredientStore.addIngredient(
                Ingredient(
                    name = "양파",
                    category = "채소",
                    percent = 100
                )
            )

            Toast.makeText(
                requireContext(),
                "영수증 재료를 냉장고에 추가했어요",
                Toast.LENGTH_SHORT
            ).show()

            (activity as MainActivity).openIngredientList()
        }

        return view
    }
}