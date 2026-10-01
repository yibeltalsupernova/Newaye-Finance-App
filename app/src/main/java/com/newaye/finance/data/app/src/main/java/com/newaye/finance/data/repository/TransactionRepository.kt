package com.newaye.finance.data.repository

import androidx.room.withTransaction
import com.newaye.finance.data.local.NewayeDatabase
import com.newaye.finance.data.local.entity.AccountEntity
import com.newaye.finance.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val database: NewayeDatabase
) {

    private val transactionDao = database.transactionDao()
    private val accountDao = database.accountDao()

    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    fun getTotalIncome(): Flow<Double> {
        return transactionDao.getTotalIncome()
    }

    fun getTotalExpenses(): Flow<Double> {
        return transactionDao.getTotalExpenses()
    }

    suspend fun addTransaction(
        transaction: TransactionEntity
    ) {
        database.withTransaction {

            val account =
                accountDao.getAccountById(transaction.accountId)
                    ?: return@withTransaction

            val newBalance =
                if (transaction.type == "INCOME") {
                    account.balance + transaction.amount
                } else {
                    account.balance - transaction.amount
                }

            accountDao.updateAccount(
                account.copy(
                    balance = newBalance
                )
            )

            transactionDao.insertTransaction(transaction)
        }
    }

    suspend fun updateTransaction(
        updatedTransaction: TransactionEntity
    ) {
        database.withTransaction {

            val oldTransaction =
                transactionDao.getTransactionById(
                    updatedTransaction.id
                )

            if (oldTransaction == null) {
                return@withTransaction
            }

            val oldAccount =
                accountDao.getAccountById(
                    oldTransaction.accountId
                )

            if (oldAccount == null) {
                return@withTransaction
            }

            // Reverse the old transaction.
            val balanceAfterReversal =
                if (oldTransaction.type == "INCOME") {
                    oldAccount.balance - oldTransaction.amount
                } else {
                    oldAccount.balance + oldTransaction.amount
                }

            accountDao.updateAccount(
                oldAccount.copy(
                    balance = balanceAfterReversal
                )
            )

            val updatedAccount =
                if (
                    updatedTransaction.accountId ==
                    oldTransaction.accountId
                ) {
                    oldAccount.copy(
                        balance = balanceAfterReversal
                    )
                } else {
                    accountDao.getAccountById(
                        updatedTransaction.accountId
                    )
                }

            if (updatedAccount != null) {

                val finalBalance =
                    if (updatedTransaction.type == "INCOME") {
                        updatedAccount.balance +
                            updatedTransaction.amount
                    } else {
                        updatedAccount.balance -
                            updatedTransaction.amount
                    }

                accountDao.updateAccount(
                    updatedAccount.copy(
                        balance = finalBalance
                    )
                )
            }

            transactionDao.updateTransaction(
                updatedTransaction
            )
        }
    }

    suspend fun deleteTransaction(
        transaction: TransactionEntity
    ) {
        database.withTransaction {

            val account =
                accountDao.getAccountById(
                    transaction.accountId
                )

            if (account != null) {

                val restoredBalance =
                    if (transaction.type == "INCOME") {
                    account.balance - transaction.amount
                } else {
                    account.balance + transaction.amount
                }

                accountDao.updateAccount(
                    account.copy(
                        balance = restoredBalance
                    )
                )
            }

            transactionDao.deleteTransaction(
                transaction
            )
        }
    }
}
