package com.example.swtermproject.ocr

data class BReceiptItemCandidate(
    val name: String,
    val amountText: String? = null,
    val amount: Double? = null,
    val unit: String? = null,
    val amountGram: Double? = null,
    val category: String = "기타",
    val amountSource: String = "unknown"
)