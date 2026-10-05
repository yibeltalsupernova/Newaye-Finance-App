
package com.newaye.finance.sms

sealed class SmsParserResult {

    data class Success(
        val transaction: ParsedSmsTransaction
    ) : SmsParserResult()

    data class NotRecognized(
        val reason: String,
        val rawMessage: String
    ) : SmsParserResult()

    data class Invalid(
        val reason: String,
        val rawMessage: String
    ) : SmsParserResult()
}
