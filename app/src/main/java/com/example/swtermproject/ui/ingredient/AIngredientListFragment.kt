package com.example.swtermproject.ui.ingredient

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.model.ATempIngredientStore
import com.example.swtermproject.data.model.Ingredient
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AIngredientListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AIngredientAdapter
    private lateinit var editSearch: EditText
    private lateinit var emptyLayout: LinearLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var spinnerSort: Spinner

    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabMenuLayout: LinearLayout

    private lateinit var btnAll: Button
    private lateinit var btnVegetable: Button
    private lateinit var btnDairy: Button
    private lateinit var btnProtein: Button

    private var isFabOpen = false

    private val filteredList = mutableListOf<Ingredient>()

    private var currentCategory = "전체"
    private var dashboardFilter = FILTER_ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dashboardFilter = arguments?.getString("filter") ?: FILTER_ALL
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_ingredient_list,
            container,
            false
        )

        recyclerView = view.findViewById(R.id.recyclerIngredient)
        editSearch = view.findViewById(R.id.editSearch)
        emptyLayout = view.findViewById(R.id.emptyLayout)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        spinnerSort = view.findViewById(R.id.spinnerSort)
        fabMain = view.findViewById(R.id.fabMain)
        fabMenuLayout = view.findViewById(R.id.fabMenuLayout)

        val btnAddManual = view.findViewById<Button>(R.id.btnAddManual)
        val btnAddBarcode = view.findViewById<Button>(R.id.btnAddBarcode)
        val btnAddReceipt = view.findViewById<Button>(R.id.btnAddReceipt)

        btnAll = view.findViewById(R.id.btnFilterAll)
        btnVegetable = view.findViewById(R.id.btnFilterVegetable)
        btnDairy = view.findViewById(R.id.btnFilterDairy)
        btnProtein = view.findViewById(R.id.btnFilterProtein)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        filteredList.clear()
        filteredList.addAll(ATempIngredientStore.ingredients)

        adapter = AIngredientAdapter(
            filteredList,
            requireActivity(),
            { position ->
                showDeleteDialog(position)
            },
            {
                refreshList()
            }
        )

        recyclerView.adapter = adapter

        val sortOptions = listOf(
            "기본순",
            "이름순",
            "부족순",
            "유통기한순",
            "즐겨찾기순"
        )

        spinnerSort.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            sortOptions
        )

        spinnerSort.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    refreshList()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        swipeRefresh.setOnRefreshListener {
            refreshList()
            swipeRefresh.isRefreshing = false
        }

        editSearch.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    refreshList()
                }

                override fun afterTextChanged(s: Editable?) {}
            }
        )

        btnAll.setOnClickListener {
            currentCategory = "전체"
            dashboardFilter = FILTER_ALL
            updateFilterButtonStyle(btnAll)
            refreshList()
        }

        btnVegetable.setOnClickListener {
            currentCategory = "채소"
            dashboardFilter = FILTER_ALL
            updateFilterButtonStyle(btnVegetable)
            refreshList()
        }

        btnDairy.setOnClickListener {
            currentCategory = "유제품"
            dashboardFilter = FILTER_ALL
            updateFilterButtonStyle(btnDairy)
            refreshList()
        }

        btnProtein.setOnClickListener {
            currentCategory = "단백질"
            dashboardFilter = FILTER_ALL
            updateFilterButtonStyle(btnProtein)
            refreshList()
        }

        fabMain.setOnClickListener {
            toggleFabMenu()
        }

        btnAddManual.setOnClickListener {
            closeFabMenu()
            (activity as MainActivity).openIngredientInput()
        }

        btnAddBarcode.setOnClickListener {
            closeFabMenu()
            (activity as MainActivity).openBarcodeScan()
        }

        btnAddReceipt.setOnClickListener {
            closeFabMenu()
            (activity as MainActivity).openReceiptScan()
        }

        applyDashboardDefaultSort()
        updateFilterButtonStyle(btnAll)
        refreshList()

        return view
    }

    private fun toggleFabMenu() {
        if (!isFabOpen) {
            fabMenuLayout.visibility = View.VISIBLE

            fabMenuLayout.alpha = 0f
            fabMenuLayout.translationY = 120f

            fabMenuLayout.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(260)
                .setInterpolator(OvershootInterpolator())
                .start()

            fabMain.animate()
                .rotation(45f)
                .setDuration(220)
                .start()
        } else {
            closeFabMenu()
        }

        isFabOpen = !isFabOpen
    }

    private fun closeFabMenu() {
        fabMenuLayout.animate()
            .alpha(0f)
            .translationY(120f)
            .setDuration(200)
            .withEndAction {
                fabMenuLayout.visibility = View.GONE
            }
            .start()

        fabMain.animate()
            .rotation(0f)
            .setDuration(220)
            .start()

        isFabOpen = false
    }

    private fun applyDashboardDefaultSort() {
        when (dashboardFilter) {
            FILTER_FAVORITE -> {
                spinnerSort.setSelection(4)
            }

            FILTER_LOW -> {
                spinnerSort.setSelection(2)
            }

            FILTER_EXPIRE -> {
                spinnerSort.setSelection(3)
            }

            else -> {
                spinnerSort.setSelection(0)
            }
        }
    }

    private fun updateFilterButtonStyle(selectedButton: Button) {
        val buttons = listOf(
            btnAll,
            btnVegetable,
            btnDairy,
            btnProtein
        )

        buttons.forEach { button ->
            val isSelected = button == selectedButton

            button.setBackgroundResource(
                if (isSelected) {
                    R.drawable.bg_chip
                } else {
                    R.drawable.bg_chip_white
                }
            )

            button.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isSelected) {
                        R.color.primary_green_dark
                    } else {
                        R.color.text_sub
                    }
                )
            )

            button.setTypeface(
                null,
                if (isSelected) {
                    Typeface.BOLD
                } else {
                    Typeface.NORMAL
                }
            )
        }
    }

    private fun refreshList() {
        val keyword = editSearch.text.toString()

        filteredList.clear()

        filteredList.addAll(
            ATempIngredientStore.ingredients.filter { ingredient ->
                val matchesKeyword = ingredient.name.contains(
                    keyword,
                    ignoreCase = true
                )

                val matchesCategory =
                    currentCategory == "전체" || ingredient.category == currentCategory

                val matchesDashboard = when (dashboardFilter) {
                    FILTER_FAVORITE -> ingredient.favorite
                    FILTER_LOW -> ingredient.percent <= 20
                    FILTER_EXPIRE -> ingredient.expireDay <= 3
                    else -> true
                }

                matchesKeyword && matchesCategory && matchesDashboard
            }
        )

        when (spinnerSort.selectedItem.toString()) {
            "이름순" -> {
                filteredList.sortBy { it.name }
            }

            "부족순" -> {
                filteredList.sortBy { it.percent }
            }

            "유통기한순" -> {
                filteredList.sortBy { it.expireDay }
            }

            "즐겨찾기순" -> {
                filteredList.sortByDescending { it.favorite }
            }
        }

        emptyLayout.visibility =
            if (filteredList.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

        adapter.notifyDataSetChanged()
    }

    private fun showDeleteDialog(position: Int) {
        if (position !in filteredList.indices) return

        AlertDialog.Builder(requireContext())
            .setTitle("재료 삭제")
            .setMessage("정말 삭제하시겠어요?")
            .setPositiveButton("삭제") { _, _ ->
                val ingredient = filteredList[position]

                ATempIngredientStore.ingredients.remove(ingredient)

                refreshList()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    companion object {
        const val FILTER_ALL = "all"
        const val FILTER_FAVORITE = "favorite"
        const val FILTER_LOW = "low"
        const val FILTER_EXPIRE = "expire"

        fun newInstance(filter: String): AIngredientListFragment {
            val fragment = AIngredientListFragment()
            val bundle = Bundle()
            bundle.putString("filter", filter)
            fragment.arguments = bundle
            return fragment
        }
    }
}