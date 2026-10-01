package com.newaye.finance.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId"]),
        Index(value = ["date"])
    ]
)
data class TransactionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val accountId: Long,

    /**
     * INCOME or EXPENSE
     */
    val type: String,

    /**
     * Positive monetary amount.
     */
    val amount: Double,

    /**
     * User-defined category.
     * Examples:
     * Salary, Food, Transport, Rent, Shopping
     */
    val category: String,

    /**
     * Optional description.
     */
    val note: String = "",

    /**
     * Transaction timestamp in milliseconds.
     */
    val date: Long = System.currentTimeMillis(),

    val currency: String = "ETB",

    val createdAt: Long = System.currentTimeMillis()
)
