package com.example.swtermproject.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R

class ARecipeCategoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_recipe_category, container, false)

        view.findViewById<Button>(R.id.btnKorean).setOnClickListener {
            (activity as MainActivity).openRecipeList("한식")
        }

        view.findViewById<Button>(R.id.btnWestern).setOnClickListener {
            (activity as MainActivity).openRecipeList("양식")
        }

        view.findViewById<Button>(R.id.btnJapanese).setOnClickListener {
            (activity as MainActivity).openRecipeList("일식")
        }

        view.findViewById<Button>(R.id.btnEtc).setOnClickListener {
            (activity as MainActivity).openRecipeList("기타")
        }

        return view
    }
}