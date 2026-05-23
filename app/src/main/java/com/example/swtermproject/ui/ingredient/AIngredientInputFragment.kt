package com.example.swtermproject.ui.ingredient

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.swtermproject.MainActivity
import com.example.swtermproject.R
import com.example.swtermproject.viewmodel.BIngredientViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class AIngredientInputFragment : Fragment() {

    private val viewModel: BIngredientViewModel by viewModels()

    private var currentAmountEditedByUser = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_ingredient_input,
            container,
            false
        )

        val editName = view.findViewById<EditText>(R.id.editIngredientName)
        val editInitialAmount = view.findViewById<EditText>(R.id.editInitialAmount)
        val editCurrentAmount = view.findViewById<EditText>(R.id.editCurrentAmount)
        val editExpiryDate = view.findViewById<EditText>(R.id.editExpiryDate)

        val spinnerCategory = view.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerUnit = view.findViewById<Spinner>(R.id.spinnerUnit)
        val spinnerStorage = view.findViewById<Spinner>(R.id.spinnerStorage)

        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnReceiptScan = view.findViewById<Button>(R.id.btnReceiptScan)
        val btnBarcodeScan = view.findViewById<Button>(R.id.btnBarcodeScan)

        setupSpinner(
            spinner = spinnerCategory,
            items = listOf("채소", "유제품", "단백질", "조미료/소스", "기타")
        )

        setupSpinner(
            spinner = spinnerUnit,
            items = listOf("개", "g", "ml", "봉", "팩")
        )

        setupSpinner(
            spinner = spinnerStorage,
            items = listOf("냉장", "냉동", "실온")
        )

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

        editExpiryDate.setOnClickListener {
            showDatePicker(editExpiryDate)
        }

        btnSave.setOnClickListener {
            val name = editName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val initialAmount = editInitialAmount.text.toString().toDoubleOrNull()
            val currentAmount = editCurrentAmount.text.toString().toDoubleOrNull()
            val unit = spinnerUnit.selectedItem.toString()
            val expiryDate = editExpiryDate.text.toString().trim()
            val storageType = spinnerStorage.selectedItem.toString()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "재료명을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (initialAmount == null || initialAmount <= 0.0) {
                Toast.makeText(requireContext(), "구매량을 올바르게 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentAmount == null || currentAmount < 0.0) {
                Toast.makeText(requireContext(), "현재 남은 양을 올바르게 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentAmount > initialAmount) {
                Toast.makeText(requireContext(), "현재 남은 양은 구매량보다 클 수 없습니다", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addIngredientManually(
                name = name,
                category = category,
                initialAmount = initialAmount,
                currentAmount = currentAmount,
                unit = unit,
                expiryDate = expiryDate,
                storageType = storageType
            )
        }

        btnReceiptScan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AReceiptScanFragment())
                .commit()
        }

        btnBarcodeScan.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ABarcodeScanFragment())
                .commit()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeMessage()
        observeSimilarIngredient()
    }

    private fun observeSimilarIngredient() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.similarIngredient.collect { state ->
                    if (state == null) return@collect

                    AlertDialog.Builder(requireContext())
                        .setTitle("비슷한 재료가 이미 있어요")
                        .setMessage(
                            "기존 재료: ${state.existing.name}\n" +
                                "새 재료: ${state.incoming.name}\n\n" +
                                "같은 재료로 보고 수량을 합칠까요?"
                        )
                        .setPositiveButton("병합") { _, _ ->
                            viewModel.resolveSimilarIngredient(merge = true)
                        }
                        .setNegativeButton("새로 추가") { _, _ ->
                            viewModel.resolveSimilarIngredient(merge = false)
                        }
                        .setOnCancelListener {
                            viewModel.clearSimilarIngredient()
                        }
                        .show()
                }
            }
        }
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

    private fun observeMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.message.collect { message ->
                    if (message == null) return@collect

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    val shouldMoveToList =
                        message.contains("추가") || message.contains("저장")

                    viewModel.clearMessage()

                    if (shouldMoveToList) {
                        (activity as MainActivity).openIngredientList()
                    }
                }
            }
        }
    }
}
