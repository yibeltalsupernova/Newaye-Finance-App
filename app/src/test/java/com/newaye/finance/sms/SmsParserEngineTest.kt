package com.newaye.finance.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserEngineTest {

    private val engine =
        SmsParserEngine(
            profiles = listOf(
                DevelopmentSmsProfile.profile
            )
        )

    @Test
    fun parsesIncomeSms() {

        val result =
            engine.parse(
                sender = "NEWAYE",
                message =
                    "Your account has been credited with ETB 5,000.00. " +
                    "Reference: ABC12345. Account: My Cash. Salary payment."
            )

        assertTrue(
            result is SmsParserResult.Success
        )

        val transaction =
            (result as SmsParserResult.Success)
                .transaction

        assertEquals(
            "INCOME",
            transaction.type
        )

        assertEquals(
            5000.0,
            transaction.amount,
            0.01
        )

        assertEquals(
            "ABC12345",
            transaction.reference
        )

        assertEquals(
            "Salary",
            transaction.categoryHint
        )
    }

    @Test
    fun parsesExpenseSms() {

        val result =
            engine.parse(
                sender = "NEWAYE",
                message =
                    "Your account was debited ETB 2,000. " +
                    "Reference: FOOD123. Account: My Cash. Food payment."
            )

        assertTrue(
            result is SmsParserResult.Success
        )

        val transaction =
            (result as SmsParserResult.Success)
                .transaction

        assertEquals(
            "EXPENSE",
            transaction.type
        )

        assertEquals(
            2000.0,
            transaction.amount,
            0.01
        )

        assertEquals(
            "FOOD123",
            transaction.reference
        )

        assertEquals(
            "Food",
            transaction.categoryHint
        )
    }

    @Test
    fun rejectsEmptyMessage() {

        val result =
            engine.parse(
                sender = "NEWAYE",
                message = ""
            )

        assertTrue(
            result is SmsParserResult.Invalid
        )
    }

    @Test
    fun rejectsUnknownSender() {

        val result =
            engine.parse(
                sender = "UNKNOWN",
                message =
                    "Your account has been credited with ETB 5,000."
            )

        assertTrue(
            result is SmsParserResult.NotRecognized
        )
    }

    @Test
    fun parsesAmountWithComma() {

        val result =
            engine.parse(
                sender = "NEWAYE",
                message =
                    "Your account has been credited with ETB 125,500.50."
            )

        assertTrue(
            result is SmsParserResult.Success
        )

        val transaction =
            (result as SmsParserResult.Success)
                .transaction

        assertEquals(
            125500.50,
            transaction.amount,
            0.01
        )
    }
}
