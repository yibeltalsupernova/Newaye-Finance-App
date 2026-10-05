
package com.newaye.finance.sms

data class ParsedSmsTransaction(
    val type: String,
    val amount: Double,
    val currency: String = "ETB",
    val sender: String? = null,
    val accountHint: String? = null,
    val categoryHint: String? = null,
    val reference: String? = null,
    val dateText: String? = null,
    val note: String = "",
    val confidence: Double = 0.0,
    val rawMessage: String
)
