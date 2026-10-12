
package com.newaye.finance.sms

/**
 * Provider-specific parsing rules for Ethiopian financial SMS messages.
 *
 * These profiles analyze message text only. They do not read the SMS inbox.
 */
object EthiopianSmsProfiles {

    val cbe = SmsParserProfile(
        id = "cbe",
        name = "Commercial Bank of Ethiopia",
        senderKeywords = listOf(
            "Commercial Bank of Ethiopia",
            "CBE"
        ),
        incomeKeywords = listOf(
            "credited",
            "received",
            "deposited"
        ),
        expenseKeywords = listOf(
            "transferred",
            "debited",
            "withdrawn",
            "paid"
        ),
        amountPatterns = listOf(
            // Prefer the amount explicitly transferred.
            Regex(
                """transferred\s+ETB\s*([0-9]+(?:,[0-9]{3})*(?:\.[0-9]+)?)""",
                RegexOption.IGNORE_CASE
            ),
            // Fallback when the SMS uses a different wording.
            Regex(
                """ETB\s*([0-9]+(?:,[0-9]{3})*(?:\.[0-9]+)?)""",
                RegexOption.IGNORE_CASE
            ),
            Regex(
                """([0-9]+(?:,[0-9]{3})*(?:\.[0-9]+)?)\s*ETB""",
                RegexOption.IGNORE_CASE
            )
        ),
        referencePatterns = listOf(
            Regex(
                """(?:transaction\s*(?:number|id)|reference|ref|txn\s*id)\s*[:#-]?\s*([A-Za-z0-9-]+)""",
                RegexOption.IGNORE_CASE
            )
        ),
        accountPatterns = listOf(
            Regex(
                """account\s+([0-9*]+)""",
                RegexOption.IGNORE_CASE
            )
        ),
        categoryKeywords = mapOf(
            "Transfer" to listOf("transferred", "transfer"),
            "Bank Fees" to listOf("service charge", "vat", "disaster recovery"),
            "Shopping" to listOf("purchase", "merchant"),
            "Salary" to listOf("salary", "payroll")
        )
    )

    val telebirr = SmsParserProfile(
        id = "telebirr",
        name = "Telebirr",
        senderKeywords = listOf(
            "telebirr",
            "transactioninfo.ethiotelecom.com.et"
        ),
        incomeKeywords = listOf(
            "received",
            "credited",
            "money received"
        ),
        expenseKeywords = listOf(
            "you have paid",
            "you have purchased",
            "you have sent",
            "payment made",
            "purchase made"
        ),
        amountPatterns = listOf(
            // Match the package/payment amount before the balance.
            Regex(
                """(?:paid|payment made|purchase made)[\s\S]{0,40}?\bETB\s*([0-9]+(?:,[0-9]{3})*(?:\.[0-9]+)?)""",
                RegexOption.IGNORE_CASE
            ),
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
                """transaction\s+(?:number|id)\s+is\s+([A-Za-z0-9-]+)""",
                RegexOption.IGNORE_CASE
            ),
            Regex(
                """(?:reference|ref)\s*[:#-]?\s*([A-Za-z0-9-]+)""",
                RegexOption.IGNORE_CASE
            )
        ),
        accountPatterns = emptyList(),
        categoryKeywords = mapOf(
            "Mobile Package" to listOf("package", "bundle", "voice weekly"),
            "Transfer" to listOf("transfer", "sent"),
            "Shopping" to listOf("purchase", "merchant")
        )
    )

    val mpesa = SmsParserProfile(
        id = "mpesa-ethiopia",
        name = "M-PESA Ethiopia",
        senderKeywords = listOf(
            "M-PESA",
            "Safaricom"
        ),
        incomeKeywords = listOf(
            "received",
            "credited",
            "money received"
        ),
        expenseKeywords = listOf(
            "you have bought",
            "you have sent",
            "you have paid",
            "payment made"
        ),
        amountPatterns = listOf(
            // Example: "bundle of 5.00 Birr".
            Regex(
                """(?:bundle|payment|purchase)\s+of\s+([0-9]+(?:,[0-9]{3})*(?:\.[0-9]+)?)\s*(?:Birr|ETB)""",
                RegexOption.IGNORE_CASE
            ),
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
                """transaction\s+(?:number|id)\s+([A-Za-z0-9-]+)""",
                RegexOption.IGNORE_CASE
            )
        ),
        accountPatterns = emptyList(),
        categoryKeywords = mapOf(
            "Mobile Package" to listOf("bundle", "airtime"),
            "Transfer" to listOf("transfer", "sent"),
            "Shopping" to listOf("purchase", "bought")
        )
    )

    /**
     * The order matters if a message ever matches more than one profile.
     */
    val all: List<SmsParserProfile> = listOf(
        cbe,
        telebirr,
        mpesa
    )
}
