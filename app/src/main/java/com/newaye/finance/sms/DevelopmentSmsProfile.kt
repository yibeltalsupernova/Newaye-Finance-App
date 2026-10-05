package com.newaye.finance.sms

object DevelopmentSmsProfile {

    val profile =
        SmsParserProfile(

            id = "development",

            name = "Development Financial SMS",

            senderKeywords = listOf(
                "NEWAYE"
            ),

            incomeKeywords = listOf(
                "credited",
                "received",
                "deposit",
                "income"
            ),

            expenseKeywords = listOf(
                "debited",
                "spent",
                "withdrawn",
                "payment",
                "purchase"
            ),

            amountPatterns = listOf(

                Regex(
                    """(?:ETB|Birr)\s*([0-9]+(?:,[0-9]{3})*(?:\.[0-9]+)?)""",
                    RegexOption.IGNORE_CASE
                ),

                Regex(
                    """([0-9]+(?:,[0-9]{3})*(?:\.[0-9]+)?)\s*(?:ETB|Birr)""",
                    RegexOption.IGNORE_CASE
                )
            ),

            referencePatterns = listOf(

                Regex(
                    """(?:reference|ref|transaction\s*id|txn\s*id)\s*[:#-]?\s*([A-Za-z0-9-]+)""",
                    RegexOption.IGNORE_CASE
                )
            ),

            accountPatterns = listOf(

                Regex(
                    """(?:account|acct)\s*[:#-]?\s*([A-Za-z0-9 *-]+)""",
                    RegexOption.IGNORE_CASE
                )
            ),

            categoryKeywords = mapOf(

                "Salary" to listOf(
                    "salary",
                    "payroll"
                ),

                "Food" to listOf(
                    "food",
                    "restaurant",
                    "lunch",
                    "dinner"
                ),

                "Transfer" to listOf(
                    "transfer",
                    "sent"
                )
            )
        )
}
