
@Test
fun parsesIncomeSms() {
    val result = engine.parse(
        sender = "NEWAYE",
        message = "Your account has been credited with ETB 5,000.00. Reference: ABC12345. Account: My Cash. Salary payment."
    )

    assertTrue(
        "Parser result: $result",
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
