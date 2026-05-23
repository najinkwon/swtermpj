package com.example.swtermproject.ui.ingredient

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.repository.BIngredientRepository
import com.example.swtermproject.domain.model.BIngredient
import kotlinx.coroutines.launch

class AReceiptResultFragment : Fragment() {

    private val itemNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val received = arguments?.getStringArrayList(ARG_ITEM_NAMES)
            ?: arrayListOf()

        itemNames.clear()
        itemNames.addAll(
            received
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
        )
    }

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

        val textSummary = view.findViewById<TextView>(R.id.textReceiptResultSummary)
        val layoutItems = view.findViewById<LinearLayout>(R.id.layoutReceiptItems)
        val btnAdd = view.findViewById<Button>(R.id.btnAddReceiptItems)

        val repository = BIngredientRepository(
            BAppDatabase.getDatabase(requireContext()).ingredientDao()
        )

        textSummary.text = "총 ${itemNames.size}개의 재료를 찾았어요"

        layoutItems.removeAllViews()

        if (itemNames.isEmpty()) {
            layoutItems.addView(
                createEmptyView()
            )
            btnAdd.isEnabled = false
        } else {
            itemNames.forEach { name ->
                layoutItems.addView(
                    createItemView(name)
                )
            }
        }

        btnAdd.setOnClickListener {
            if (itemNames.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "추가할 재료가 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnAdd.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                runCatching {
                    itemNames.forEach { name ->
                        repository.addIngredient(
                            BIngredient(
                                name = name,
                                category = categoryForName(name),
                                initialAmount = 1.0,
                                currentAmount = 1.0,
                                unit = "개",
                                expiryDate = "",
                                storageType = "냉장"
                            )
                        )
                    }
                }.onSuccess {
                    Toast.makeText(
                        requireContext(),
                        "영수증 재료를 냉장고에 추가했어요",
                        Toast.LENGTH_SHORT
                    ).show()

                    (activity as MainActivity).openIngredientList()
                }.onFailure {
                    btnAdd.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        it.message ?: "영수증 재료 추가에 실패했습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return view
    }

    private fun createItemView(name: String): View {
        val context = requireContext()
        val category = categoryForName(name)

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(18.dp(), 18.dp(), 18.dp(), 18.dp())
            setBackgroundResource(R.drawable.bg_card)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 14.dp())
            layoutParams = params
            elevation = 3f
        }

        val emoji = TextView(context).apply {
            text = emojiForCategory(category)
            textSize = 26f
            gravity = android.view.Gravity.CENTER
            setBackgroundResource(R.drawable.bg_chip)
            layoutParams = LinearLayout.LayoutParams(
                52.dp(),
                52.dp()
            )
        }

        val textWrap = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            params.setMargins(14.dp(), 0, 0, 0)
            layoutParams = params
        }

        val title = TextView(context).apply {
            text = name
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setTextColor(resources.getColor(R.color.text_main, null))
        }

        val sub = TextView(context).apply {
            text = "$category · OCR 인식 후보"
            textSize = 14f
            setTextColor(resources.getColor(R.color.text_sub, null))
            setPadding(0, 4.dp(), 0, 0)
        }

        textWrap.addView(title)
        textWrap.addView(sub)

        row.addView(emoji)
        row.addView(textWrap)

        return row
    }

    private fun createEmptyView(): View {
        return TextView(requireContext()).apply {
            text = "인식된 재료 후보가 없습니다"
            textSize = 16f
            setTextColor(resources.getColor(R.color.text_sub, null))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 32.dp(), 0, 32.dp())
        }
    }

    private fun categoryForName(name: String): String {
        return when {
            name.contains("우유") ||
                name.contains("치즈") ||
                name.contains("요거트") ||
                name.contains("요구르트") -> "유제품"

            name.contains("계란") ||
                name.contains("달걀") ||
                name.contains("고기") ||
                name.contains("닭") ||
                name.contains("돼지") ||
                name.contains("소고기") ||
                name.contains("참치") ||
                name.contains("두부") -> "단백질"

            name.contains("양파") ||
                name.contains("대파") ||
                name.contains("파") ||
                name.contains("마늘") ||
                name.contains("상추") ||
                name.contains("채소") ||
                name.contains("야채") -> "채소"

            name.contains("간장") ||
                name.contains("고추장") ||
                name.contains("된장") ||
                name.contains("소스") ||
                name.contains("드레싱") -> "조미료/소스"

            else -> "기타"
        }
    }

    private fun emojiForCategory(category: String): String {
        return when (category) {
            "채소" -> "🥬"
            "유제품" -> "🥛"
            "단백질" -> "🥚"
            "조미료/소스" -> "🥫"
            else -> "🍽️"
        }
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val ARG_ITEM_NAMES = "item_names"

        fun newInstance(names: List<String>): AReceiptResultFragment {
            val fragment = AReceiptResultFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(
                ARG_ITEM_NAMES,
                ArrayList(names)
            )
            fragment.arguments = bundle
            return fragment
        }
    }
}
