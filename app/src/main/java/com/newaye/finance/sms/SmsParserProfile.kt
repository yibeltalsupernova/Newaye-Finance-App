
package com.newaye.finance.sms

data class SmsParserProfile(
    val id: String,
    val name: String,

    val senderKeywords: List<String> = emptyList(),

    val incomeKeywords: List<String> = emptyList(),

    val expenseKeywords: List<String> = emptyList(),

    val amountPatterns: List<Regex> = emptyList(),

    val referencePatterns: List<Regex> = emptyList(),

    val accountPatterns: List<Regex> = emptyList(),

    val categoryKeywords: Map<String, List<String>> = emptyMap()
)
