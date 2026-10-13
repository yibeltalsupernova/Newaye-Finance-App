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

        val totalDeducted = extractAmountFromPatterns(
            normalizedMessage,
            profile.totalAmountPatterns
        )
        val explicitFeeAmount = extractFeeAmount(
            normalizedMessage,
            profile
        )
        val feeAmount = when {
            totalDeducted != null && totalDeducted >= amount ->
                (totalDeducted - amount).coerceAtLeast(0.0)
            explicitFeeAmount != null -> explicitFeeAmount
            else -> null
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
                feeAmount = feeAmount,
                totalDeducted = totalDeducted ?: (feeAmount?.let { amount + it }),
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
    return extractAmountFromPatterns(message, profile.amountPatterns)
}

private fun extractAmountFromPatterns(
    message: String,
    patterns: List<Regex>
): Double? {
    for (pattern in patterns) {
        val match = pattern.find(message) ?: continue
        val captured = match.groupValues.getOrNull(1)
        val numberText = (captured ?: match.value)
            .replace("ETB", "", ignoreCase = true)
            .replace("Birr", "", ignoreCase = true)
            .replace(",", "")
            .trim()
        val amount = numberText.toDoubleOrNull()
            ?: Regex("""[0-9]+(?:\.[0-9]+)?""")
                .find(numberText)?.value?.toDoubleOrNull()
        if (amount != null && amount > 0.0) return amount
    }
    return null
}

private fun extractFeeAmount(
    message: String,
    profile: SmsParserProfile
): Double? {
    if (profile.feePatterns.isEmpty()) return null

    var total = 0.0
    var foundAny = false
    for (pattern in profile.feePatterns) {
        for (match in pattern.findAll(message)) {
            val captured = match.groupValues.getOrNull(1) ?: continue
            val value = captured.replace(",", "").toDoubleOrNull() ?: continue
            if (value >= 0.0) {
                total += value
                foundAny = true
            }
        }
    }
    return if (foundAny) total else null
}

private fun extractReference(
    message: String,
    profile: SmsParserProfile
): String? {
    for (pattern in profile.referencePatterns) {
        val match = pattern.find(message)
            ?: continue

        // Return the first capture group when one exists.
        if (match.groupValues.size > 1) {
            val captured = match.groupValues[1].trim()

            if (captured.isNotBlank()) {
                return captured
            }
        }

        // Safe fallback for patterns without capture groups.
        return match.value.trim()
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

        if (match.groupValues.size > 1) {
            val captured = match.groupValues[1].trim()

            if (captured.isNotBlank()) {
                return captured
            }
        }

        return match.value.trim()
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
