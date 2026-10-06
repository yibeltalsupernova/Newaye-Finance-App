package com.newaye.finance.sms

class SmsParserEngine(
    private val profiles: List<SmsParserProfile>
) {

    fun parse(
        sender: String?,
        message: String
    ): SmsParserResult {

        val normalizedMessage = normalize(message)

        if (normalizedMessage.isBlank()) {
            return SmsParserResult.Invalid(
                reason = "SMS message is empty.",
                rawMessage = message
            )
        }

        val profile = findProfile(sender, normalizedMessage)

        if (profile == null) {
            return SmsParserResult.NotRecognized(
                reason = "No parser profile matched this SMS.",
                rawMessage = message
            )
        }

        val type = detectTransactionType(
            normalizedMessage,
            profile
        )

        if (type == null) {
            return SmsParserResult.NotRecognized(
                reason = "Could not determine transaction type.",
                rawMessage = message
            )
        }

        val amount = extractAmount(
            normalizedMessage,
            profile
        )

        if (amount == null || amount <= 0.0) {
            return SmsParserResult.Invalid(
                reason = "Could not extract a valid transaction amount.",
                rawMessage = message
            )
        }

        val reference = extractReference(
            normalizedMessage,
            profile
        )

        val accountHint = extractAccountHint(
            normalizedMessage,
            profile
        )

        val categoryHint = detectCategory(
            normalizedMessage,
            profile
        )

        val confidence = calculateConfidence(
            sender = sender,
            type = type,
            amount = amount,
            reference = reference,
            accountHint = accountHint
        )

        return SmsParserResult.Success(
            ParsedSmsTransaction(
                type = type,
                amount = amount,
                currency = "ETB",
                sender = sender,
                accountHint = accountHint,
                categoryHint = categoryHint,
                reference = reference,
                note = message.trim(),
                confidence = confidence,
                rawMessage = message
            )
        )
    }

    private fun findProfile(
        sender: String?,
        message: String
    ): SmsParserProfile? {

        return profiles.firstOrNull { profile ->

            val senderMatches =
                sender != null &&
                    profile.senderKeywords.any { keyword ->
                        sender.contains(
                            keyword,
                            ignoreCase = true
                        )
                    }

            val messageMatches =
                profile.senderKeywords.any { keyword ->
                    message.contains(
                        keyword,
                        ignoreCase = true
                    )
                }

            senderMatches || messageMatches
        }
    }

    private fun detectTransactionType(
        message: String,
        profile: SmsParserProfile
    ): String? {

        val income =
            profile.incomeKeywords.any { keyword ->
                message.contains(
                    keyword,
                    ignoreCase = true
                )
            }

        val expense =
            profile.expenseKeywords.any { keyword ->
                message.contains(
                    keyword,
                    ignoreCase = true
                )
            }

        return when {
            income && !expense -> "INCOME"
            expense && !income -> "EXPENSE"
            else -> null
        }
    }

    private fun extractAmount(
        message: String,
        profile: SmsParserProfile
    ): Double? {

        for (pattern in profile.amountPatterns) {

            val match = pattern.find(message)
                ?: continue

            val text = match.value
                .replace(
                    "ETB",
                    "",
                    ignoreCase = true
                )
                .replace(
                    "Birr",
                    "",
                    ignoreCase = true
                )
                .replace(",", "")
                .trim()

            val number =
                Regex(
                    """[0-9]+(?:\.[0-9]+)?"""
                ).find(text)?.value

            val amount =
                number?.toDoubleOrNull()

            if (amount != null) {
                return amount
            }
        }

        return null
    }

    private fun extractReference(
        message: String,
        profile: SmsParserProfile
    ): String? {

        for (pattern in profile.referencePatterns) {

            val match = pattern.find(message)
                ?: continue

            val text = match.value

            val separator =
                Regex(
                    """[:#-]"""
                ).find(text)

            if (separator != null) {
                return text
                    .substring(
                        separator.range.last + 1
                    )
                    .trim()
            }

            return text.trim()
        }

        return null
    }

    private fun extractAccountHint(
        message: String,
        profile: SmsParserProfile
    ): String? {

        for (pattern in profile.accountPatterns) {

            val match = pattern.find(message)
                ?: continue

            val text = match.value

            val separator =
                Regex(
                    """[:#-]"""
                ).find(text)

            if (separator != null) {
                return text
                    .substring(
                        separator.range.last + 1
                    )
                    .trim()
                    .removeSuffix(".")
            }

            return text.trim()
        }

        return null
    }

    private fun detectCategory(
        message: String,
        profile: SmsParserProfile
    ): String? {

        for ((category, keywords) in profile.categoryKeywords) {

            val found =
                keywords.any { keyword ->
                    message.contains(
                        keyword,
                        ignoreCase = true
                    )
                }

            if (found) {
                return category
            }
        }

        return null
    }

    private fun calculateConfidence(
        sender: String?,
        type: String,
        amount: Double?,
        reference: String?,
        accountHint: String?
    ): Double {

        var score = 0.0

        if (!sender.isNullOrBlank()) {
            score += 0.15
        }

        if (
            type == "INCOME" ||
            type == "EXPENSE"
        ) {
            score += 0.35
        }

        if (
            amount != null &&
            amount > 0.0
        ) {
            score += 0.30
        }

        if (!reference.isNullOrBlank()) {
            score += 0.10
        }

        if (!accountHint.isNullOrBlank()) {
            score += 0.10
        }

        return score.coerceIn(0.0, 1.0)
    }

    private fun normalize(
        message: String
    ): String {

        return message
            .replace(
                "\u00A0",
                " "
            )
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()
    }
}
