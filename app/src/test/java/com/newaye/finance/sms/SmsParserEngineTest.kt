
package com.newaye.finance.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserEngineTest {

  
private val engine = SmsParserEngine(
    profiles = EthiopianSmsProfiles.all +
        DevelopmentSmsProfile.profile
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
    
@Test
fun parsesTelebirrPackagePurchase() {
    val result = engine.parse(
        sender = "127",
        message = "Dear Customer. You have paid ETB 25.00 for package Voice Weekly. Your transaction number is DJC2PTRK1U. Your current balance is ETB 22.08. Thank you for using telebirr Ethio telecom."
    )

    assertTrue(
        "Expected Success but got: $result",
        result is SmsParserResult.Success
    )

    val transaction =
        (result as SmsParserResult.Success).transaction

    assertEquals("EXPENSE", transaction.type)
    assertEquals(25.0, transaction.amount, 0.01)
    assertEquals("DJC2PTRK1U", transaction.reference)
    assertEquals("Mobile Package", transaction.categoryHint)
}

@Test
fun parsesCbeTransferWithoutConfusingFeesOrBalance() {
    val result = engine.parse(
        sender = "CBE",
        message = "Dear Customer. You have successfully transferred ETB550.00 from account 1*********9164 to account 1*********8111. Service charge of ETB 0.50 and VAT(15%) of ETB0.08 and Disaster Recovery(5%) of 0.03 with total of ETB550.61. Your current balance is ETB1,066.23. Thanks for Banking with CBE."
    )

    assertTrue(
        "Expected Success but got: $result",
        result is SmsParserResult.Success
    )

    val transaction =
        (result as SmsParserResult.Success).transaction

    assertEquals("EXPENSE", transaction.type)
    assertEquals(550.0, transaction.amount, 0.01)
    assertEquals("Transfer", transaction.categoryHint)
    assertEquals("CBE transfer amount", 550.0, transaction.amount, 0.01)
    assertEquals("CBE fee amount", 0.61, transaction.feeAmount!!, 0.01)
    assertEquals("CBE total deduction", 550.61, transaction.totalDeducted!!, 0.01)
    assertTrue(
        "CBE example should not invent a reference",
        transaction.reference == null
    )
}

@Test
fun parsesMpesaBundlePurchase() {
    val result = engine.parse(
        sender = "MPESA",
        message = "Dear Customer, you have bought a Safaricom bundle of 5.00 Birr for 251712973146. Transaction number TCQ2QUJ58E. Your current M-PESA balance is 120.97 Birr."
    )

    assertTrue(
        "Expected Success but got: $result",
        result is SmsParserResult.Success
    )

    val transaction =
        (result as SmsParserResult.Success).transaction

    assertEquals("EXPENSE", transaction.type)
    assertEquals(5.0, transaction.amount, 0.01)
    assertEquals("TCQ2QUJ58E", transaction.reference)
    assertEquals("Mobile Package", transaction.categoryHint)
}


@Test
fun parsesCbeIncomingTransfer() {
    val result = engine.parse(
        sender = "CBE",
        message = "Dear Customer, your account has been credited ETB 1,250.00. Current balance ETB 4,500.00. Thanks for Banking with CBE."
    )

    assertTrue(
        "Expected Success but got: $result",
        result is SmsParserResult.Success
    )
    val transaction = (result as SmsParserResult.Success).transaction
    assertEquals("INCOME", transaction.type)
    assertEquals(1250.0, transaction.amount, 0.01)
    assertEquals(null, transaction.feeAmount)
}

@Test
fun doesNotTreatCbeBalanceAsTransferAmount() {
    val result = engine.parse(
        sender = "CBE",
        message = "You have successfully transferred ETB550.00. Total of ETB550.61. Your current balance is ETB1,066.23. Thanks for Banking with CBE."
    )

    assertTrue(
        "Expected Success but got: $result",
        result is SmsParserResult.Success
    )
    val transaction = (result as SmsParserResult.Success).transaction
    assertEquals(550.0, transaction.amount, 0.01)
    assertEquals(550.61, transaction.totalDeducted!!, 0.01)
    assertEquals(0.61, transaction.feeAmount!!, 0.01)
}

}
