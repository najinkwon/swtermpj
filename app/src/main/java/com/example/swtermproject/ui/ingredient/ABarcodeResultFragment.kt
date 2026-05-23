package com.example.swtermproject.ui.ingredient

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.remote.barcode.BBarcodeProduct
import com.example.swtermproject.data.remote.barcode.BFoodsafetyBarcodeClient
import com.example.swtermproject.data.repository.BBarcodeRepository
import com.example.swtermproject.data.repository.BIngredientRepository
import com.example.swtermproject.domain.model.BIngredient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class ABarcodeResultFragment : Fragment() {

    private var barcode: String = ""
    private var currentProduct: BBarcodeProduct? = null
    private var currentAmountEditedByUser = false

    private val barcodeRepository =
        BBarcodeRepository(BFoodsafetyBarcodeClient.api)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        barcode = arguments?.getString("barcode") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_barcode_result,
            container,
            false
        )

        val loadingLayout = view.findViewById<LinearLayout>(R.id.loadingLayout)
        val contentLayout = view.findViewById<View>(R.id.contentLayout)
        val productCard = view.findViewById<LinearLayout>(R.id.productCard)
        val errorCard = view.findViewById<LinearLayout>(R.id.errorCard)

        val lottieLoading = view.findViewById<View>(R.id.lottieLoading)
        val progressFallback = view.findViewById<ProgressBar>(R.id.progressFallback)

        setupLoadingFallback(lottieLoading, progressFallback)

        val textBarcodeNumber = view.findViewById<TextView>(R.id.textBarcodeNumber)
        val textProductEmoji = view.findViewById<TextView>(R.id.textProductEmoji)
        val textProductName = view.findViewById<TextView>(R.id.textProductName)
        val textCompany = view.findViewById<TextView>(R.id.textCompany)
        val textProductCategory = view.findViewById<TextView>(R.id.textProductCategory)
        val textErrorMessage = view.findViewById<TextView>(R.id.textErrorMessage)

        val editInitialAmount = view.findViewById<EditText>(R.id.editBarcodeInitialAmount)
        val editCurrentAmount = view.findViewById<EditText>(R.id.editBarcodeCurrentAmount)
        val editExpiryDate = view.findViewById<EditText>(R.id.editBarcodeExpiryDate)
        val spinnerUnit = view.findViewById<Spinner>(R.id.spinnerBarcodeUnit)
        val spinnerStorage = view.findViewById<Spinner>(R.id.spinnerBarcodeStorage)

        val btnAdd = view.findViewById<Button>(R.id.btnAddBarcodeIngredient)
        val btnRetry = view.findViewById<Button>(R.id.btnRetryBarcode)
        val btnManualInput = view.findViewById<Button>(R.id.btnManualInput)

        setupSpinner(
            spinner = spinnerUnit,
            items = listOf("개", "g", "ml", "봉", "팩")
        )

        setupSpinner(
            spinner = spinnerStorage,
            items = listOf("냉장", "냉동", "실온")
        )

        setupAmountAutoFill(
            editInitialAmount = editInitialAmount,
            editCurrentAmount = editCurrentAmount
        )

        editExpiryDate.setOnClickListener {
            showDatePicker(editExpiryDate)
        }

        val ingredientRepository = BIngredientRepository(
            BAppDatabase.getDatabase(requireContext()).ingredientDao()
        )

        contentLayout.visibility = View.INVISIBLE

        fun showLoading() {
            loadingLayout.visibility = View.VISIBLE
            contentLayout.visibility = View.INVISIBLE
            btnAdd.isEnabled = false
        }

        fun showContent() {
            loadingLayout.visibility = View.GONE
            contentLayout.visibility = View.VISIBLE

            val fadeIn = AlphaAnimation(0f, 1f)
            fadeIn.duration = 350
            contentLayout.startAnimation(fadeIn)
        }

        fun showProduct(product: BBarcodeProduct) {
            currentProduct = product

            productCard.visibility = View.VISIBLE
            errorCard.visibility = View.GONE

            textProductEmoji.text = emojiForCategory(product.category)
            textProductName.text = product.name
            textCompany.text = product.company
            textProductCategory.text = product.category

            btnAdd.isEnabled = true
        }

        fun showError(message: String) {
            currentProduct = null

            productCard.visibility = View.GONE
            errorCard.visibility = View.VISIBLE
            textErrorMessage.text = message

            btnAdd.isEnabled = false
        }

        fun loadProduct() {
            showLoading()
            textBarcodeNumber.text = "바코드 번호: $barcode"

            viewLifecycleOwner.lifecycleScope.launch {
                delay(400)

                val result = runCatching {
                    barcodeRepository.searchProduct(barcode)
                }

                val product = result.getOrNull()

                if (product != null) {
                    showProduct(product)
                } else {
                    val message = result.exceptionOrNull()?.message
                        ?: "식품안전나라 API에서 해당 바코드 상품 정보를 찾지 못했어요.\n스캔한 바코드 번호를 확인한 뒤 직접 입력으로 등록할 수 있어요."

                    showError(message)
                }

                showContent()
            }
        }

        btnAdd.setOnClickListener {
            val product = currentProduct

            if (product == null) {
                Toast.makeText(
                    requireContext(),
                    "추가할 상품 정보가 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val initialAmount = editInitialAmount.text.toString().toDoubleOrNull()
            val currentAmount = editCurrentAmount.text.toString().toDoubleOrNull()
            val unit = spinnerUnit.selectedItem.toString()
            val expiryDate = editExpiryDate.text.toString().trim()
            val storageType = spinnerStorage.selectedItem.toString()

            if (initialAmount == null || initialAmount <= 0.0) {
                Toast.makeText(
                    requireContext(),
                    "구매량을 올바르게 입력하세요",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (currentAmount == null || currentAmount < 0.0) {
                Toast.makeText(
                    requireContext(),
                    "현재 남은 양을 올바르게 입력하세요",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (currentAmount > initialAmount) {
                Toast.makeText(
                    requireContext(),
                    "현재 남은 양은 구매량보다 클 수 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            btnAdd.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                runCatching {
                    ingredientRepository.addIngredient(
                        BIngredient(
                            name = product.name,
                            category = product.category,
                            initialAmount = initialAmount,
                            currentAmount = currentAmount,
                            unit = unit,
                            expiryDate = expiryDate,
                            storageType = storageType
                        )
                    )
                }.onSuccess {
                    Toast.makeText(
                        requireContext(),
                        "${product.name} 추가 완료!",
                        Toast.LENGTH_SHORT
                    ).show()

                    (activity as MainActivity).openIngredientList()
                }.onFailure {
                    btnAdd.isEnabled = true

                    Toast.makeText(
                        requireContext(),
                        it.message ?: "상품 추가에 실패했습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        btnRetry.setOnClickListener {
            loadProduct()
        }

        btnManualInput.setOnClickListener {
            (activity as MainActivity).openIngredientInput()
        }

        loadProduct()

        return view
    }

    private fun setupSpinner(
        spinner: Spinner,
        items: List<String>
    ) {
        spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            items
        )
    }

    private fun setupAmountAutoFill(
        editInitialAmount: EditText,
        editCurrentAmount: EditText
    ) {
        editCurrentAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                currentAmountEditedByUser = true
            }
        }

        editInitialAmount.addTextChangedListener(
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
                    if (!currentAmountEditedByUser || editCurrentAmount.text.isNullOrBlank()) {
                        editCurrentAmount.setText(s?.toString().orEmpty())
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            }
        )
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

    private fun emojiForCategory(category: String): String {
        return when (category) {
            "채소" -> "🥬"
            "유제품" -> "🥛"
            "단백질" -> "🥚"
            "조미료/소스" -> "🥫"
            else -> "📦"
        }
    }

    private fun setupLoadingFallback(
        lottieView: View,
        fallbackProgress: ProgressBar
    ) {
        lottieView.visibility = View.GONE
        fallbackProgress.visibility = View.VISIBLE
    }

    companion object {
        fun newInstance(barcode: String): ABarcodeResultFragment {
            val fragment = ABarcodeResultFragment()
            val bundle = Bundle()
            bundle.putString("barcode", barcode)
            fragment.arguments = bundle
            return fragment
        }
    }
}
