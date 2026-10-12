
package com.newaye.finance.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserEngineTest {

    private val engine = SmsParserEngine(
        profiles = listOf(
            DevelopmentSmsProfile.profile
        )
    )

    @Test
    fun parsesIncomeSms() {
        val result = engine.parse(
            sender = "NEWAYE",
            message = "Your account has been credited with ETB 5,000.00. Reference: ABC12345. Account: My Cash. Salary payment."
        )

        assertTrue(
            "Expected Success but got: $result",
            result is SmsParserResult.Success
        )

        val transaction =
            (result as SmsParserResult.Success).transaction

        assertEquals(
            "Incorrect transaction type",
            "INCOME",
            transaction.type
        )

        assertEquals(
            "Incorrect amount",
            5000.0,
            transaction.amount,
            0.01
        )

        assertEquals(
            "Incorrect reference",
            "ABC12345",
            transaction.reference
        )

        assertEquals(
            "Incorrect category",
            "Salary",
            transaction.categoryHint
        )
    }

    @Test
    fun parsesExpenseSms() {
        val result = engine.parse(
            sender = "NEWAYE",
            message = "Your account was debited ETB 2,000. Reference: FOOD123. Account: My Cash. Food payment."
        )

        assertTrue(
            "Expected Success but got: $result",
            result is SmsParserResult.Success
        )

        val transaction =
            (result as SmsParserResult.Success).transaction

        assertEquals(
            "Incorrect transaction type",
            "EXPENSE",
            transaction.type
        )

        assertEquals(
            "Incorrect amount",
            2000.0,
            transaction.amount,
            0.01
        )

        assertEquals(
            "Incorrect reference",
            "FOOD123",
            transaction.reference
        )

        assertEquals(
            "Incorrect category",
            "Food",
            transaction.categoryHint
        )
    }

    @Test
    fun rejectsEmptyMessage() {
        val result = engine.parse(
            sender = "NEWAYE",
            message = ""
        )

        assertTrue(
            "An empty message should be Invalid",
            result is SmsParserResult.Invalid
        )
    }

    @Test
    fun rejectsUnknownSender() {
        val result = engine.parse(
            sender = "UNKNOWN",
            message = "Your account has been credited with ETB 5,000."
        )

        assertTrue(
            "An unknown sender should be NotRecognized",
            result is SmsParserResult.NotRecognized
        )
    }

    @Test
    fun parsesAmountWithComma() {
        val result = engine.parse(
            sender = "NEWAYE",
            message = "Your account has been credited with ETB 125,500.50."
        )

        assertTrue(
            "Expected Success but got: $result",
            result is SmsParserResult.Success
        )

        val transaction =
            (result as SmsParserResult.Success).transaction

        assertEquals(
            "Incorrect comma-formatted amount",
            125500.50,
            transaction.amount,
            0.01
        )
    }
}
