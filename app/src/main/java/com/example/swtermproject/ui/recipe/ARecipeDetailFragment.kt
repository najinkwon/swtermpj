package com.example.swtermproject.ui.recipe

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
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
import com.example.swtermproject.domain.model.BRecipe
import com.example.swtermproject.domain.model.BRecipeIngredientStatus
import com.example.swtermproject.recipe.BRecipeDataSource
import com.example.swtermproject.recipe.BRecipeScorer
import com.example.swtermproject.ui.shopping.AShoppingFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ARecipeDetailFragment : Fragment() {

    private var recipeName: String = "계란볶음밥"
    private val currentMissingIngredients = mutableListOf<String>()

    private lateinit var ingredientRepository: BIngredientRepository

    private lateinit var textReason: TextView
    private lateinit var textMatch: TextView
    private lateinit var textIngredients: TextView
    private lateinit var btnCookDone: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recipeName = arguments?.getString("recipeName") ?: "계란볶음밥"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_recipe_detail,
            container,
            false
        )

        val recipe = BRecipeDataSource.recipes.find { it.title == recipeName }
            ?: BRecipeDataSource.recipes.first()

        ingredientRepository = BIngredientRepository(
            BAppDatabase.getDatabase(requireContext()).ingredientDao()
        )

        val textImageEmoji = view.findViewById<TextView>(R.id.textRecipeImageEmoji)
        val textTitle = view.findViewById<TextView>(R.id.textRecipeTitle)
        textReason = view.findViewById(R.id.textRecipeReason)
        textMatch = view.findViewById(R.id.textMatch)
        val textCookTime = view.findViewById<TextView>(R.id.textCookTime)
        val textDifficulty = view.findViewById<TextView>(R.id.textDifficulty)
        textIngredients = view.findViewById(R.id.textIngredients)
        val textSteps = view.findViewById<TextView>(R.id.textSteps)
        val btnYoutube = view.findViewById<Button>(R.id.btnYoutube)
        val btnShopping = view.findViewById<Button>(R.id.btnShopping)
        btnCookDone = view.findViewById(R.id.btnCookDone)

        listOf(btnYoutube, btnShopping, btnCookDone).forEach {
            it.backgroundTintList = null
        }

        btnYoutube.setBackgroundResource(R.drawable.bg_youtube_button)
        btnYoutube.setTextColor(resources.getColor(R.color.white, null))
        btnShopping.setBackgroundResource(R.drawable.bg_chip_white)
        btnShopping.setTextColor(resources.getColor(R.color.primary_green_dark, null))

        textImageEmoji.text = emojiForRecipe(recipe)
        textTitle.text = recipe.title
        textReason.text = "냉장고 재료를 기준으로 매칭률을 계산하고 있어요."
        textMatch.text = "계산 중"
        textCookTime.text = cookTimeForRecipe(recipe)
        textDifficulty.text = difficultyForRecipe(recipe)

        textIngredients.text = BRecipeScorer.getRequirements(recipe)
            .joinToString("\n") { requirement ->
                "• ${requirement.name} ${formatAmount(requirement.amount)}${requirement.unit} 필요"
            }

        textSteps.text = recipe.description

        loadRecipeMatch(recipe)

        btnYoutube.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "https://www.youtube.com/results?search_query=${recipe.youtubeKeyword}"
                )
            )

            startActivity(intent)
        }

        btnShopping.setOnClickListener {
            if (currentMissingIngredients.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "이 레시피는 부족한 필수 재료가 없어요",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                openRecipeShopping(
                    recipeName = recipe.title,
                    missingIngredients = ArrayList(currentMissingIngredients)
                )
            }
        }

        btnCookDone.setOnClickListener {
            showCookDoneConfirmDialog(recipe)
        }

        return view
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun loadRecipeMatch(recipe: BRecipe) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val ingredients = ingredientRepository.getAllIngredients()
                val scoredRecipe = BRecipeScorer.scoreRecipe(recipe, ingredients)

                scoredRecipe to ingredients
            }

            bindRecipeMatch(
                recipe = result.first,
                ownedIngredients = result.second
            )
        }
    }

    private fun openRecipeShopping(
        recipeName: String,
        missingIngredients: ArrayList<String>
    ) {
        val hostActivity = activity

        if (hostActivity is MainActivity) {
            hostActivity.openShoppingForRecipe(
                recipeName = recipeName,
                missingIngredients = missingIngredients
            )
            return
        }

        parentFragmentManager.beginTransaction()
            .replace(
                R.id.singleFragmentContainer,
                AShoppingFragment.newRecipeMode(
                    recipeName = recipeName,
                    missingIngredients = missingIngredients
                )
            )
            .addToBackStack(null)
            .commit()
    }

    private fun updateCookDoneButtonStyle(canCook: Boolean) {
        if (canCook) {
            btnCookDone.text = "요리했어요"
            btnCookDone.setBackgroundResource(R.drawable.bg_primary_button)
            btnCookDone.setTextColor(android.graphics.Color.WHITE)
            btnCookDone.alpha = 1.0f
        } else {
            btnCookDone.text = "요리했어요"
            btnCookDone.setBackgroundResource(R.drawable.bg_outline_button)
            btnCookDone.setTextColor(
                resources.getColor(R.color.primary_green_dark, null)
            )
            btnCookDone.alpha = 1.0f
        }
    }

    private fun bindRecipeMatch(
        recipe: BRecipe,
        ownedIngredients: List<BIngredient>
    ) {
        val statuses =
            if (recipe.ingredientStatuses.isNotEmpty()) {
                recipe.ingredientStatuses
            } else {
                BRecipeScorer.scoreRecipe(recipe, ownedIngredients).ingredientStatuses
            }

        val essentialStatuses = statuses.filter { it.requirement.essential }
        val essentialMainAndSub = essentialStatuses.filter {
            it.requirement.group == "main" || it.requirement.group == "sub"
        }

        val missingEssential = essentialStatuses
            .filterNot { it.isEnough }

        val canCook = missingEssential.isEmpty()
        updateCookDoneButtonStyle(canCook)

        currentMissingIngredients.clear()
        currentMissingIngredients.addAll(
            missingEssential
                .map { it.requirement.name }
                .distinct()
        )

        val matchPercent =
            if (essentialMainAndSub.isEmpty()) {
                100
            } else {
                val enoughCount = essentialMainAndSub.count { it.isEnough }

                ((enoughCount.toDouble() / essentialMainAndSub.size.toDouble()) * 100.0)
                    .toInt()
                    .coerceIn(0, 100)
            }

        textMatch.text = "재료 ${matchPercent}%"

        textReason.text =
            if (missingEssential.isEmpty()) {
                "필수 재료가 충분해요. 요리 후 '요리했어요'를 누르면 사용량만큼 재고가 차감돼요."
            } else {
                "부족 재료: ${
                    missingEssential.joinToString(", ") {
                        "${it.requirement.name} ${formatAmount(it.missingAmount)}${it.requirement.unit}"
                    }
                }"
            }

        textIngredients.text = statuses.joinToString("\n") { status ->
            formatIngredientStatusLine(status)
        }
    }

    private fun formatIngredientStatusLine(status: BRecipeIngredientStatus): String {
        val requirement = status.requirement
        val requiredText = "${formatAmount(requirement.amount)}${requirement.unit}"

        val label =
            when (requirement.group) {
                "main" -> "주재료"
                "sub" -> "부재료"
                "seasoning" -> "양념"
                else -> "재료"
            }

        val prefix =
            when {
                status.isEnough -> "보유"
                status.isOwned && !status.isUnitCompatible -> "확인"
                !requirement.essential -> "선택"
                else -> "부족"
            }

        return when {
            status.isEnough -> {
                val ownedText = "${formatAmount(status.ownedAmount)}${status.ownedUnit}"
                "$prefix · ${requirement.name}  $requiredText 필요 / 보유 $ownedText"
            }

            status.isOwned && !status.isUnitCompatible -> {
                val ownedText = "${formatAmount(status.ownedAmount)}${status.ownedUnit}"
                "$prefix · ${requirement.name}  $requiredText 필요 / 보유 $ownedText · 단위 확인"
            }

            !requirement.essential -> {
                "$prefix · ${requirement.name}  $requiredText 필요 · $label"
            }

            else -> {
                "$prefix · ${requirement.name}  $requiredText 필요"
            }
        }
    }

    private fun showCookDoneConfirmDialog(recipe: BRecipe) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val ingredients = ingredientRepository.getAllIngredients()
                val scoredRecipe = BRecipeScorer.scoreRecipe(recipe, ingredients)

                scoredRecipe to ingredients
            }

            val scoredRecipe = result.first
            val ingredients = result.second
            val statuses = scoredRecipe.ingredientStatuses

            val missingEssential = statuses.filter {
                it.requirement.essential && !it.isEnough
            }

            if (missingEssential.isNotEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "필수 재료가 부족해서 재고를 차감할 수 없어요",
                    Toast.LENGTH_SHORT
                ).show()

                bindRecipeMatch(
                    recipe = scoredRecipe,
                    ownedIngredients = ingredients
                )
                return@launch
            }

            val consumableStatuses = statuses.filter {
                it.isEnough &&
                        it.isUnitCompatible &&
                        it.ownedIngredientId != null
            }

            if (consumableStatuses.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "차감할 재료가 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            val message = consumableStatuses.joinToString("\n") { status ->
                "- ${status.requirement.name}: ${formatAmount(status.requirement.amount)}${status.requirement.unit} 차감"
            }

            showCookDoneDialog(
                message = message,
                onConfirm = {
                    consumeRecipeIngredients(recipe, consumableStatuses)
                }
            )
        }
    }

    private fun showCookDoneDialog(
        message: String,
        onConfirm: () -> Unit
    ) {
        val context = requireContext()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24.dp(), 22.dp(), 24.dp(), 10.dp())
        }

        val title = TextView(context).apply {
            text = "요리했어요?"
            textSize = 21f
            setTypeface(null, Typeface.BOLD)
            setTextColor(resources.getColor(R.color.text_main, null))
            includeFontPadding = false
        }

        val description = TextView(context).apply {
            text = "사용한 재료를 냉장고 재고에서 차감할게요."
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_sub, null))
            setPadding(0, 8.dp(), 0, 0)
            includeFontPadding = false
        }

        val infoBox = TextView(context).apply {
            text = message
            textSize = 15f
            setTextColor(resources.getColor(R.color.text_main, null))
            setBackgroundResource(R.drawable.bg_dialog_info_box)
            setPadding(16.dp(), 14.dp(), 16.dp(), 14.dp())
            setLineSpacing(6f, 1.0f)
            includeFontPadding = false
        }

        val infoParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 18.dp()
        }

        val buttonRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 20.dp(), 0, 0)
        }

        val cancelButton = makeDialogButton(
            text = "취소",
            textColor = R.color.primary_green_dark,
            background = R.drawable.bg_dialog_outline_button
        )

        val confirmButton = makeDialogButton(
            text = "차감하기",
            textColor = R.color.white,
            background = R.drawable.bg_primary_button
        )

        buttonRow.addView(
            cancelButton,
            LinearLayout.LayoutParams(
                0,
                48.dp(),
                1f
            ).apply {
                rightMargin = 8.dp()
            }
        )

        buttonRow.addView(
            confirmButton,
            LinearLayout.LayoutParams(
                0,
                48.dp(),
                1f
            ).apply {
                leftMargin = 8.dp()
            }
        )

        container.addView(title)
        container.addView(description)
        container.addView(infoBox, infoParams)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(context)
            .setView(container)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        dialog.show()
    }

    private fun makeDialogButton(
        text: String,
        textColor: Int,
        background: Int
    ): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(resources.getColor(textColor, null))
            setBackgroundResource(background)
        }
    }

    private fun consumeRecipeIngredients(
        recipe: BRecipe,
        statuses: List<BRecipeIngredientStatus>
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    statuses.forEach { status ->
                        val ingredientId = status.ownedIngredientId
                            ?: return@forEach

                        val newAmount =
                            (status.ownedAmount - status.requirement.amount)
                                .coerceAtLeast(0.0)

                        ingredientRepository.updateCurrentAmount(
                            id = ingredientId,
                            currentAmount = newAmount
                        )
                    }
                }
            }

            result.onSuccess {
                Toast.makeText(
                    requireContext(),
                    "요리에 사용한 재료를 차감했어요",
                    Toast.LENGTH_SHORT
                ).show()

                loadRecipeMatch(recipe)
            }.onFailure {
                Toast.makeText(
                    requireContext(),
                    it.message ?: "재고 차감에 실패했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun emojiForRecipe(recipe: BRecipe): String {
        val source = "${recipe.title} ${recipe.category}"

        return when {
            source.contains("볶음밥") -> "🍳"
            source.contains("🍚") -> "🍚"
            source.contains("파스타") -> "🍝"
            source.contains("두부") -> "🥘"
            source.contains("규동") -> "🍱"
            source.contains("오므라이스") -> "🍳"
            source.contains("샐러드") -> "🥗"
            source.contains("샌드위치") -> "🥪"
            else -> "🍽️"
        }
    }

    private fun cookTimeForRecipe(recipe: BRecipe): String {
        return when {
            recipe.title.contains("간장계란밥") -> "5분"
            recipe.title.contains("볶음밥") -> "10분"
            recipe.title.contains("두부") -> "15분"
            recipe.title.contains("파스타") -> "20분"
            recipe.title.contains("오므라이스") -> "20분"
            recipe.title.contains("샐러드") -> "10분"
            recipe.title.contains("샌드위치") -> "10분"
            else -> "15분"
        }
    }

    private fun difficultyForRecipe(recipe: BRecipe): String {
        return when {
            recipe.title.contains("간장계란밥") -> "쉬움"
            recipe.title.contains("계란볶음밥") -> "쉬움"
            recipe.title.contains("샐러드") -> "쉬움"
            recipe.title.contains("샌드위치") -> "쉬움"
            else -> "보통"
        }
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

    companion object {
        fun newInstance(recipeName: String): ARecipeDetailFragment {
            val fragment = ARecipeDetailFragment()
            val bundle = Bundle()
            bundle.putString("recipeName", recipeName)
            fragment.arguments = bundle
            return fragment
        }
    }
}