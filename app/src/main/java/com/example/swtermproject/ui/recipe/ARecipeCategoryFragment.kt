package com.example.swtermproject.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R

class ARecipeCategoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_recipe_category,
            container,
            false
        )

        view.findViewById<LinearLayout>(R.id.cardKorean).setOnClickListener {
            (activity as MainActivity).openRecipeList("한식")
        }

        view.findViewById<LinearLayout>(R.id.cardWestern).setOnClickListener {
            (activity as MainActivity).openRecipeList("양식")
        }

        view.findViewById<LinearLayout>(R.id.cardJapanese).setOnClickListener {
            (activity as MainActivity).openRecipeList("일식")
        }

        view.findViewById<LinearLayout>(R.id.cardEtc).setOnClickListener {
            (activity as MainActivity).openRecipeList("기타")
        }

        view.findViewById<Button>(R.id.btnAiRecommend).apply {
            text = "냉장고 재료로 추천 보기"
            setOnClickListener {
                (activity as MainActivity).openRecipeList("기타")
            }
        }

        return view
    }
}
