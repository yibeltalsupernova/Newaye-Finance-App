package com.newaye.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.newaye.finance.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions " +
        "WHERE accountId = :accountId " +
        "ORDER BY date DESC"
    )
    fun getTransactionsForAccount(
        accountId: Long
    ): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions " +
        "WHERE id = :id LIMIT 1"
    )
    suspend fun getTransactionById(
        id: Long
    ): TransactionEntity?

    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) " +
        "FROM transactions WHERE type = 'INCOME'"
    )
    fun getTotalIncome(): Flow<Double>

    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) " +
        "FROM transactions WHERE type = 'EXPENSE'"
    )
    fun getTotalExpenses(): Flow<Double>

    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) " +
        "FROM transactions " +
        "WHERE accountId = :accountId AND type = 'INCOME'"
    )
    fun getAccountIncome(
        accountId: Long
    ): Flow<Double>

    @Query(
        "SELECT COALESCE(SUM(amount), 0.0) " +
        "FROM transactions " +
        "WHERE accountId = :accountId AND type = 'EXPENSE'"
    )
    fun getAccountExpenses(
        accountId: Long
    ): Flow<Double>

    @Insert
    suspend fun insertTransaction(
        transaction: TransactionEntity
    ): Long

    @Update
    suspend fun updateTransaction(
        transaction: TransactionEntity
    )

    @Delete
    suspend fun deleteTransaction(
        transaction: TransactionEntity
    )
}
