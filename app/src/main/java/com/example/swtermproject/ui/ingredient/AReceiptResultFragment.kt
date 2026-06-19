package com.example.swtermproject.ui.ingredient

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.repository.BIngredientRepository
import com.example.swtermproject.domain.model.BIngredient
import com.example.swtermproject.ocr.BAmountNormalizer
import com.example.swtermproject.ocr.BIngredientDictionary
import com.example.swtermproject.ocr.BReceiptItemCandidate
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class AReceiptResultFragment : Fragment() {

    private val candidates = mutableListOf<ReceiptCandidate>()

    private lateinit var textSummary: TextView
    private lateinit var layoutItems: LinearLayout
    private lateinit var btnAdd: Button

    data class ReceiptCandidate(
        val name: String,
        val category: String,
        val initialAmount: Double,
        val currentAmount: Double,
        val unit: String,
        val expiryDate: String,
        val storageType: String,
        val amountSource: String = "unknown"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receivedNames = arguments?.getStringArrayList(ARG_ITEM_NAMES)
            ?: arrayListOf()

        val receivedCategories = arguments?.getStringArrayList(ARG_ITEM_CATEGORIES)
            ?: arrayListOf()

        val receivedAmounts = arguments?.getDoubleArray(ARG_ITEM_AMOUNTS)

        val receivedUnits = arguments?.getStringArrayList(ARG_ITEM_UNITS)
            ?: arrayListOf()

        candidates.clear()

        candidates.addAll(
            receivedNames
                .mapIndexed { index, rawName ->
                    val amountArg = receivedAmounts
                        ?.getOrNull(index)
                        ?.takeIf { it > 0.0 }

                    createCandidateFromArgument(
                        rawName = rawName,
                        categoryArg = receivedCategories.getOrNull(index),
                        amountArg = amountArg,
                        unitArg = receivedUnits.getOrNull(index)
                    )
                }
                .filter { it.name.isNotBlank() }
                .distinctBy { it.name }
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

        textSummary = view.findViewById(R.id.textReceiptResultSummary)
        layoutItems = view.findViewById(R.id.layoutReceiptItems)
        btnAdd = view.findViewById(R.id.btnAddReceiptItems)

        val btnAddCandidate =
            view.findViewById<Button>(R.id.btnAddReceiptCandidate)

        val repository = BIngredientRepository(
            BAppDatabase.getDatabase(requireContext()).ingredientDao()
        )

        renderItems()

        btnAddCandidate.setOnClickListener {
            showCandidateEditDialog(
                title = "재료 후보 직접 추가",
                targetIndex = null,
                initialCandidate = ReceiptCandidate(
                    name = "",
                    category = "기타",
                    initialAmount = 1.0,
                    currentAmount = 1.0,
                    unit = "개",
                    expiryDate = "",
                    storageType = "냉장",
                    amountSource = "manual"
                )
            )
        }

        btnAdd.setOnClickListener {
            if (candidates.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "추가할 재료가 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnAdd.isEnabled = false
            processReceiptCandidateSave(
                index = 0,
                repository = repository
            )
        }

        return view
    }

    private fun createCandidateFromArgument(
        rawName: String,
        categoryArg: String?,
        amountArg: Double?,
        unitArg: String?
    ): ReceiptCandidate {
        val rawText = rawName.trim()

        val dictionaryEntry = BIngredientDictionary.findBest(rawText)
        val normalizedAmount = BAmountNormalizer.extractFromText(rawText)

        val finalName = dictionaryEntry?.canonical
            ?: rawText
                .replace(
                    Regex("\\d+(\\.\\d+)?\\s*(g|kg|ml|l|L|개|입|봉|팩|묶음|통|병|캔|ea|EA|근|구|알)")
                    , ""
                )
                .replace(Regex("\\d+\\s*/\\s*\\d*"), "")
                .trim()

        val finalCategory = normalizeCategory(
            categoryArg
                ?: dictionaryEntry?.category
                ?: categoryForName(finalName)
        )

        val unit = when {
            !unitArg.isNullOrBlank() -> unitArg
            normalizedAmount != null -> normalizedAmount.unit
            else -> BAmountNormalizer.defaultUnitForIngredient(finalName)
        }

        val amount = when {
            amountArg != null && amountArg > 0.0 -> amountArg
            normalizedAmount != null && normalizedAmount.amount > 0.0 -> normalizedAmount.amount
            else -> BAmountNormalizer.defaultAmountForUnit(unit)
        }

        return ReceiptCandidate(
            name = finalName,
            category = finalCategory,
            initialAmount = amount,
            currentAmount = amount,
            unit = unit,
            expiryDate = "",
            storageType = "냉장",
            amountSource = when {
                amountArg != null -> "receipt"
                normalizedAmount != null -> normalizedAmount.source
                else -> "default"
            }
        )
    }

    private fun processReceiptCandidateSave(
        index: Int,
        repository: BIngredientRepository
    ) {
        if (index >= candidates.size) {
            Toast.makeText(
                requireContext(),
                "영수증 재료를 냉장고에 추가했어요",
                Toast.LENGTH_SHORT
            ).show()

            openIngredientListSafely()
            return
        }

        val candidate = candidates[index]
        val incomingIngredient = candidate.toIngredient()

        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                repository.findSimilarIngredientByName(incomingIngredient.name)
            }.onSuccess { similar ->
                if (similar == null) {
                    saveReceiptCandidate(
                        index = index,
                        repository = repository,
                        incomingIngredient = incomingIngredient,
                        forceNew = false
                    )
                } else {
                    showReceiptMergeDialog(
                        index = index,
                        repository = repository,
                        existingIngredient = similar,
                        incomingIngredient = incomingIngredient
                    )
                }
            }.onFailure {
                btnAdd.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    it.message ?: "중복 재료 확인에 실패했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showReceiptMergeDialog(
        index: Int,
        repository: BIngredientRepository,
        existingIngredient: BIngredient,
        incomingIngredient: BIngredient
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("비슷한 재료가 이미 있어요")
            .setMessage(
                "기존 재료: ${existingIngredient.name}\n" +
                        "새 재료: ${incomingIngredient.name}\n\n" +
                        "같은 재료로 보고 수량을 합칠까요?"
            )
            .setPositiveButton("병합") { _, _ ->
                mergeReceiptCandidate(
                    index = index,
                    repository = repository,
                    existingIngredient = existingIngredient,
                    incomingIngredient = incomingIngredient
                )
            }
            .setNegativeButton("새로 추가") { _, _ ->
                saveReceiptCandidate(
                    index = index,
                    repository = repository,
                    incomingIngredient = incomingIngredient,
                    forceNew = true
                )
            }
            .setOnCancelListener {
                btnAdd.isEnabled = true
            }
            .show()
    }

    private fun mergeReceiptCandidate(
        index: Int,
        repository: BIngredientRepository,
        existingIngredient: BIngredient,
        incomingIngredient: BIngredient
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                repository.mergeIngredientWithExisting(
                    existingId = existingIngredient.id,
                    incoming = incomingIngredient
                )
            }.onSuccess {
                processReceiptCandidateSave(
                    index = index + 1,
                    repository = repository
                )
            }.onFailure {
                btnAdd.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    it.message ?: "재료 병합에 실패했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveReceiptCandidate(
        index: Int,
        repository: BIngredientRepository,
        incomingIngredient: BIngredient,
        forceNew: Boolean
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                if (forceNew) {
                    repository.addIngredientAsNew(incomingIngredient)
                } else {
                    repository.addIngredient(incomingIngredient)
                }
            }.onSuccess {
                processReceiptCandidateSave(
                    index = index + 1,
                    repository = repository
                )
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

    private fun ReceiptCandidate.toIngredient(): BIngredient {
        return BIngredient(
            name = name,
            category = category,
            initialAmount = initialAmount,
            currentAmount = currentAmount,
            unit = unit,
            expiryDate = expiryDate,
            storageType = storageType
        )
    }

    private fun renderItems() {
        textSummary.text = "총 ${candidates.size}개의 재료 후보를 확인했어요"
        layoutItems.removeAllViews()

        if (candidates.isEmpty()) {
            layoutItems.addView(createEmptyView())
            btnAdd.isEnabled = false
        } else {
            btnAdd.isEnabled = true
            candidates.forEachIndexed { index, candidate ->
                layoutItems.addView(createItemView(index, candidate))
            }
        }
    }

    private fun createItemView(
        index: Int,
        candidate: ReceiptCandidate
    ): View {
        val context = requireContext()

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16.dp(), 16.dp(), 12.dp(), 16.dp())
            setBackgroundResource(R.drawable.bg_card)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 12.dp())
            layoutParams = params
            elevation = 0f
        }

        val emoji = TextView(context).apply {
            text = emojiForCategory(candidate.category)
            textSize = 24f
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.bg_chip)
            layoutParams = LinearLayout.LayoutParams(
                50.dp(),
                50.dp()
            )
        }

        val textWrap = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL

            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            params.setMargins(13.dp(), 0, 8.dp(), 0)
            layoutParams = params
        }

        val title = TextView(context).apply {
            text = candidate.name
            textSize = 19f
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setTypeface(null, Typeface.BOLD)
            setTextColor(resources.getColor(R.color.text_main, null))
        }

        val detail = TextView(context).apply {
            val expiryText =
                if (candidate.expiryDate.isBlank()) {
                    "유통기한 미입력"
                } else {
                    candidate.expiryDate
                }

            val amountText = formatAmountWithUnit(
                currentAmount = candidate.currentAmount,
                initialAmount = candidate.initialAmount,
                unit = candidate.unit
            )

            text = "${candidate.category} · $amountText · $expiryText · ${candidate.storageType}"
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_sub, null))
            setPadding(0, 4.dp(), 0, 0)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
        }

        val buttonWrap = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }

        val editButton = TextView(context).apply {
            text = "정보"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(resources.getColor(R.color.primary_green_dark, null))
            setBackgroundResource(R.drawable.bg_chip)
            setPadding(11.dp(), 6.dp(), 11.dp(), 6.dp())
            setOnClickListener {
                showCandidateEditDialog(
                    title = "재료 정보 입력",
                    targetIndex = index,
                    initialCandidate = candidate
                )
            }
        }

        val deleteButton = TextView(context).apply {
            text = "삭제"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setTextColor(resources.getColor(R.color.accent_red, null))
            setBackgroundResource(R.drawable.bg_stat_red)
            setPadding(11.dp(), 6.dp(), 11.dp(), 6.dp())

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 7.dp(), 0, 0)
            layoutParams = params

            setOnClickListener {
                candidates.removeAt(index)
                renderItems()
            }
        }

        textWrap.addView(title)
        textWrap.addView(detail)

        buttonWrap.addView(editButton)
        buttonWrap.addView(deleteButton)

        row.addView(emoji)
        row.addView(textWrap)
        row.addView(buttonWrap)

        return row
    }

    private fun showCandidateEditDialog(
        title: String,
        targetIndex: Int?,
        initialCandidate: ReceiptCandidate
    ) {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(42, 18, 42, 0)
        }

        val editName = EditText(context).apply {
            hint = "재료명"
            setText(initialCandidate.name)
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
        }

        val categories = listOf("채소", "유제품", "단백질", "조미료/소스", "기타")
        val spinnerCategory = Spinner(context).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )
            setSelection(
                categories.indexOf(initialCandidate.category).takeIf { it >= 0 }
                    ?: categories.lastIndex
            )
        }

        val editInitialAmount = EditText(context).apply {
            hint = "구매량"
            setText(formatAmount(initialCandidate.initialAmount))
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine(true)
        }

        val editCurrentAmount = EditText(context).apply {
            hint = "현재 남은 양"
            setText(formatAmount(initialCandidate.currentAmount))
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setSingleLine(true)
        }

        val units = listOf("g", "개", "ml", "봉", "팩")
        val spinnerUnit = Spinner(context).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                units
            )
            setSelection(
                units.indexOf(initialCandidate.unit).takeIf { it >= 0 } ?: 0
            )
        }

        val editExpiryDate = EditText(context).apply {
            hint = "유통기한 선택"
            setText(initialCandidate.expiryDate)
            inputType = InputType.TYPE_NULL
            isFocusable = false
            isClickable = true
            setSingleLine(true)
            setOnClickListener {
                showDatePicker(this)
            }
        }

        val storageTypes = listOf("냉장", "냉동", "실온")
        val spinnerStorage = Spinner(context).apply {
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                storageTypes
            )
            setSelection(
                storageTypes.indexOf(initialCandidate.storageType).takeIf { it >= 0 } ?: 0
            )
        }

        container.addView(makeLabel("재료명"))
        container.addView(editName)
        container.addView(makeLabel("카테고리"))
        container.addView(spinnerCategory)
        container.addView(makeLabel("구매량"))
        container.addView(editInitialAmount)
        container.addView(makeLabel("현재 남은 양"))
        container.addView(editCurrentAmount)
        container.addView(makeLabel("단위"))
        container.addView(spinnerUnit)
        container.addView(makeLabel("유통기한"))
        container.addView(editExpiryDate)
        container.addView(makeLabel("보관 방식"))
        container.addView(spinnerStorage)

        AlertDialog.Builder(context)
            .setTitle(title)
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

                        val updated = ReceiptCandidate(
                            name = name,
                            category = spinnerCategory.selectedItem.toString(),
                            initialAmount = initialAmount,
                            currentAmount = currentAmount,
                            unit = spinnerUnit.selectedItem.toString(),
                            expiryDate = editExpiryDate.text.toString().trim(),
                            storageType = spinnerStorage.selectedItem.toString(),
                            amountSource = "manual"
                        )

                        if (targetIndex == null) {
                            if (candidates.any { it.name.equals(updated.name, ignoreCase = true) }) {
                                Toast.makeText(
                                    context,
                                    "이미 추가된 후보입니다",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@setOnClickListener
                            }

                            candidates.add(updated)
                        } else if (targetIndex in candidates.indices) {
                            candidates[targetIndex] = updated
                        }

                        renderItems()
                        dismiss()
                    }
                }

                show()
            }
    }

    private fun showDatePicker(editExpiryDate: EditText) {
        val calendar = Calendar.getInstance(Locale.KOREA)

        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val dateText = String.format(
                    Locale.KOREA,
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    dayOfMonth
                )

                editExpiryDate.setText(dateText)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.show()
    }

    private fun makeLabel(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 13f
            setPadding(0, 14, 0, 4)
        }
    }

    private fun createEmptyView(): View {
        return TextView(requireContext()).apply {
            text = "인식된 재료 후보가 없습니다.\n직접 후보를 추가할 수 있어요."
            textSize = 16f
            setTextColor(resources.getColor(R.color.text_sub, null))
            gravity = Gravity.CENTER
            setPadding(0, 32.dp(), 0, 32.dp())
            setLineSpacing(6f, 1.0f)
        }
    }

    private fun openIngredientListSafely() {
        val hostActivity = activity

        if (hostActivity is MainActivity) {
            hostActivity.openIngredientList()
            return
        }

        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            putExtra(
                MainActivity.EXTRA_START_DESTINATION,
                MainActivity.DEST_INGREDIENT_LIST
            )
        }

        startActivity(intent)
        requireActivity().finish()
    }

    private fun categoryForName(name: String): String {
        val dictionaryEntry = BIngredientDictionary.findBest(name)
        if (dictionaryEntry != null) {
            return normalizeCategory(dictionaryEntry.category)
        }

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
                    name.contains("두부") ||
                    name.contains("가슴살") -> "단백질"

            name.contains("양파") ||
                    name.contains("대파") ||
                    name.contains("파") ||
                    name.contains("마늘") ||
                    name.contains("상추") ||
                    name.contains("양상추") ||
                    name.contains("양배추") ||
                    name.contains("적양배추") ||
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

    private fun normalizeCategory(category: String): String {
        return when (category.trim()) {
            "조미료" -> "조미료/소스"
            "소스" -> "조미료/소스"
            "육류" -> "단백질"
            "해산물" -> "단백질"
            "가공식품" -> "기타"
            "과일" -> "기타"
            else -> category.trim().ifBlank { "기타" }
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

    private fun formatAmountWithUnit(
        currentAmount: Double,
        initialAmount: Double,
        unit: String
    ): String {
        return "${formatAmount(currentAmount)}$unit / ${formatAmount(initialAmount)}$unit"
    }

    private fun formatAmount(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            String.format(Locale.KOREA, "%.1f", value)
                .trimEnd('0')
                .trimEnd('.')
        }
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val ARG_ITEM_NAMES = "item_names"
        private const val ARG_ITEM_CATEGORIES = "item_categories"
        private const val ARG_ITEM_AMOUNTS = "item_amounts"
        private const val ARG_ITEM_UNITS = "item_units"

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

        fun newInstanceFromCandidates(
            candidates: List<BReceiptItemCandidate>
        ): AReceiptResultFragment {
            val fragment = AReceiptResultFragment()
            val bundle = Bundle()

            bundle.putStringArrayList(
                ARG_ITEM_NAMES,
                ArrayList(candidates.map { it.name })
            )

            bundle.putStringArrayList(
                ARG_ITEM_CATEGORIES,
                ArrayList(candidates.map { it.category })
            )

            bundle.putDoubleArray(
                ARG_ITEM_AMOUNTS,
                candidates.map {
                    val amount = it.amount ?: it.amountGram ?: 0.0
                    if (amount > 0.0) amount else 0.0
                }.toDoubleArray()
            )

            bundle.putStringArrayList(
                ARG_ITEM_UNITS,
                ArrayList(
                    candidates.map {
                        it.unit
                            ?: if ((it.amountGram ?: 0.0) > 0.0) "g"
                            else BAmountNormalizer.defaultUnitForIngredient(it.name)
                    }
                )
            )

            fragment.arguments = bundle
            return fragment
        }
    }
}