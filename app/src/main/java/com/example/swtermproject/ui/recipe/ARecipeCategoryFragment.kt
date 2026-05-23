package com.example.swtermproject.ui.recipe

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.repository.BIngredientRepository
import com.example.swtermproject.recipe.BLocalFridgeRecipeGenerator
import kotlinx.coroutines.launch

class ARecipeCategoryFragment : Fragment() {

    private lateinit var btnAiRecommend: Button

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

        addAiRecommendButton(view)

        return view
    }

    private fun addAiRecommendButton(view: View) {
        val scrollView = view as? ScrollView ?: return
        val rootLayout = scrollView.getChildAt(0) as? LinearLayout ?: return

        btnAiRecommend = Button(requireContext()).apply {
            text = "AI 냉털 추천 받기"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(resources.getColor(R.color.white, null))
            setBackgroundResource(R.drawable.bg_primary_button)
            setOnClickListener {
                requestAiRecommendation()
            }
        }

        val buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            56.dp()
        ).apply {
            setMargins(0, 22.dp(), 0, 0)
        }

        rootLayout.addView(btnAiRecommend, buttonParams)

        val helperText = TextView(requireContext()).apply {
            text = "현재 냉장고 재료를 기반으로 냉털 레시피를 추천해요"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(resources.getColor(R.color.text_sub, null))
            setPadding(0, 8.dp(), 0, 0)
        }

        rootLayout.addView(
            helperText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun requestAiRecommendation() {
        btnAiRecommend.isEnabled = false
        btnAiRecommend.text = "냉털 레시피 생성 중..."

        val ingredientRepository = BIngredientRepository(
            BAppDatabase.getDatabase(requireContext()).ingredientDao()
        )

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                val ingredients = ingredientRepository.getAllIngredients()
                BLocalFridgeRecipeGenerator.generate(ingredients)
            }.onSuccess { result ->
                showAiResultDialog(result)
            }.onFailure { exception ->
                Toast.makeText(
                    requireContext(),
                    exception.message ?: "냉털 추천 생성에 실패했습니다",
                    Toast.LENGTH_LONG
                ).show()
            }

            btnAiRecommend.isEnabled = true
            btnAiRecommend.text = "AI 냉털 추천 받기"
        }
    }

    private fun showAiResultDialog(result: String) {
        val scrollView = ScrollView(requireContext())

        val textView = TextView(requireContext()).apply {
            text = result
            textSize = 15f
            setTextColor(resources.getColor(R.color.text_main, null))
            setPadding(24.dp(), 18.dp(), 24.dp(), 18.dp())
            setLineSpacing(6f, 1.0f)
        }

        scrollView.addView(textView)

        AlertDialog.Builder(requireContext())
            .setTitle("AI 냉털 추천")
            .setView(scrollView)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
