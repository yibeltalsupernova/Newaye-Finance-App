
package com.newaye.finance.sms

data class ParsedSmsTransaction(
    val type: String,
    // Main transaction amount, excluding fees when the SMS states them separately.
    val amount: Double,
    val currency: String = "ETB",
    val feeAmount: Double? = null,
    val totalDeducted: Double? = null,
    val sender: String? = null,
    val accountHint: String? = null,
    val categoryHint: String? = null,
    val reference: String? = null,
    val dateText: String? = null,
    val note: String = "",
    val confidence: Double = 0.0,
    val rawMessage: String
)
