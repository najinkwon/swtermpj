package com.example.swtermproject.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.swtermproject.ocr.BReceiptItemCandidate
import com.example.swtermproject.ocr.BReceiptOcrManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BReceiptViewModel(application: Application) : AndroidViewModel(application) {
    private val ocrManager = BReceiptOcrManager(application)

    private val _candidates = MutableStateFlow<List<BReceiptItemCandidate>>(emptyList())
    val candidates: StateFlow<List<BReceiptItemCandidate>> = _candidates

    fun extractFromRawText(rawText: String) {
        _candidates.value = ocrManager.extractItemsFromText(rawText)
    }
}
