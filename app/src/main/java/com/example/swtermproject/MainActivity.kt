package com.example.swtermproject

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.launch
import com.example.swtermproject.demo.DemoDataSeeder
import androidx.lifecycle.lifecycleScope
import com.example.swtermproject.ui.activity.RecipeDetailActivity
import com.example.swtermproject.ui.home.AHomeFragment
import com.example.swtermproject.ui.ingredient.ABarcodeScanFragment
import com.example.swtermproject.ui.ingredient.AIngredientInputFragment
import com.example.swtermproject.ui.ingredient.AIngredientListFragment
import com.example.swtermproject.ui.ingredient.AReceiptScanFragment
import com.example.swtermproject.ui.notification.ANotificationFragment
import com.example.swtermproject.ui.recipe.ARecipeCategoryFragment
import com.example.swtermproject.ui.recipe.ARecipeDetailFragment
import com.example.swtermproject.ui.recipe.ARecipeListFragment
import com.example.swtermproject.ui.shopping.AShoppingFragment

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_START_DESTINATION = "extra_start_destination"
        const val DEST_INGREDIENT_LIST = "ingredient_list"
        const val DEST_INGREDIENT_INPUT = "ingredient_input"
    }

    private lateinit var tabHome: LinearLayout
    private lateinit var tabIngredient: LinearLayout
    private lateinit var tabRecipe: LinearLayout
    private lateinit var tabNotification: LinearLayout

    private lateinit var iconHome: TextView
    private lateinit var iconIngredient: TextView
    private lateinit var iconRecipe: TextView
    private lateinit var iconNotification: TextView

    private lateinit var textHome: TextView
    private lateinit var textIngredient: TextView
    private lateinit var textRecipe: TextView
    private lateinit var textNotification: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initBottomTabs()

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                DemoDataSeeder.seedIfNeeded(this@MainActivity)
                handleStartDestination()
            }
        }

        tabHome.setOnClickListener {
            replaceFragment(AHomeFragment())
            selectTab("home")
        }

        tabIngredient.setOnClickListener {
            replaceFragment(AIngredientListFragment())
            selectTab("ingredient")
        }

        tabRecipe.setOnClickListener {
            replaceFragment(ARecipeCategoryFragment())
            selectTab("recipe")
        }

        tabNotification.setOnClickListener {
            replaceFragment(ANotificationFragment())
            selectTab("notification")
        }
    }

    private fun handleStartDestination() {
        when (intent.getStringExtra(EXTRA_START_DESTINATION)) {
            DEST_INGREDIENT_INPUT -> {
                replaceFragment(AIngredientInputFragment())
                selectTab("ingredient")
            }

            DEST_INGREDIENT_LIST -> {
                replaceFragment(AIngredientListFragment())
                selectTab("ingredient")
            }

            else -> {
                replaceFragment(AHomeFragment())
                selectTab("home")
            }
        }
    }

    private fun initBottomTabs() {
        tabHome = findViewById(R.id.tabHome)
        tabIngredient = findViewById(R.id.tabIngredient)
        tabRecipe = findViewById(R.id.tabRecipe)
        tabNotification = findViewById(R.id.tabNotification)

        iconHome = findViewById(R.id.iconHome)
        iconIngredient = findViewById(R.id.iconIngredient)
        iconRecipe = findViewById(R.id.iconRecipe)
        iconNotification = findViewById(R.id.iconNotification)

        textHome = findViewById(R.id.textHome)
        textIngredient = findViewById(R.id.textIngredient)
        textRecipe = findViewById(R.id.textRecipe)
        textNotification = findViewById(R.id.textNotification)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun moveFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun selectTab(tab: String) {
        resetTabs()

        when (tab) {
            "home" -> {
                setSelectedTab(iconHome, textHome)
            }

            "ingredient" -> {
                setSelectedTab(iconIngredient, textIngredient)
            }

            "recipe" -> {
                setSelectedTab(iconRecipe, textRecipe)
            }

            "notification" -> {
                setSelectedTab(iconNotification, textNotification)
            }
        }
    }

    private fun resetTabs() {
        val unselectedColor =
            ContextCompat.getColor(this, R.color.text_sub)

        iconHome.background = null
        iconIngredient.background = null
        iconRecipe.background = null
        iconNotification.background = null

        iconHome.setTextColor(unselectedColor)
        iconIngredient.setTextColor(unselectedColor)
        iconRecipe.setTextColor(unselectedColor)
        iconNotification.setTextColor(unselectedColor)

        textHome.setTextColor(unselectedColor)
        textIngredient.setTextColor(unselectedColor)
        textRecipe.setTextColor(unselectedColor)
        textNotification.setTextColor(unselectedColor)

        textHome.setTypeface(null, android.graphics.Typeface.NORMAL)
        textIngredient.setTypeface(null, android.graphics.Typeface.NORMAL)
        textRecipe.setTypeface(null, android.graphics.Typeface.NORMAL)
        textNotification.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun setSelectedTab(
        icon: TextView,
        text: TextView
    ) {
        val selectedColor =
            ContextCompat.getColor(this, R.color.primary_green_dark)

        icon.background = null
        icon.setTextColor(selectedColor)
        text.setTextColor(selectedColor)
        text.setTypeface(null, android.graphics.Typeface.BOLD)

        icon.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(120)
            .withEndAction {
                icon.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start()
            }
            .start()
    }

    fun openHome() {
        moveFragment(AHomeFragment())
        selectTab("home")
    }

    fun openIngredientList() {
        moveFragment(AIngredientListFragment())
        selectTab("ingredient")
    }

    fun openIngredientListWithFilter(filter: String) {
        moveFragment(AIngredientListFragment.newInstance(filter))
        selectTab("ingredient")
    }

    fun openIngredientInput() {
        moveFragment(AIngredientInputFragment())
        selectTab("ingredient")
    }

    fun openBarcodeScan() {
        moveFragment(ABarcodeScanFragment())
        selectTab("ingredient")
    }

    fun openReceiptScan() {
        moveFragment(AReceiptScanFragment())
        selectTab("ingredient")
    }

    fun openRecipeCategory() {
        moveFragment(ARecipeCategoryFragment())
        selectTab("recipe")
    }

    fun openRecipeList(category: String) {
        moveFragment(ARecipeListFragment.newInstance(category))
        selectTab("recipe")
    }

    fun openRecipeDetail(recipeName: String) {
        val intent = Intent(
            this,
            RecipeDetailActivity::class.java
        ).apply {
            putExtra(RecipeDetailActivity.EXTRA_RECIPE_NAME, recipeName)
        }

        startActivity(intent)
    }

    fun openShopping() {
        moveFragment(AShoppingFragment())
        selectTab("ingredient")
    }

    fun openShoppingForRecipe(
        recipeName: String,
        missingIngredients: ArrayList<String>
    ) {
        moveFragment(
            AShoppingFragment.newRecipeMode(
                recipeName = recipeName,
                missingIngredients = missingIngredients
            )
        )
        selectTab("ingredient")
    }
}