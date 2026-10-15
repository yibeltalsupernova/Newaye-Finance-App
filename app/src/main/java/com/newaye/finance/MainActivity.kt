@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.newaye.finance
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.newaye.finance.data.local.NewayeDatabase
import com.newaye.finance.data.local.entity.AccountEntity
import com.newaye.finance.data.local.entity.CategoryEntity
import com.newaye.finance.data.local.entity.TransactionEntity
import com.newaye.finance.data.repository.TransactionRepository
import com.newaye.finance.ui.TransactionViewModel
import com.newaye.finance.ui.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import kotlin.math.abs

private val DeepGreen = androidx.compose.ui.graphics.Color(0xFF004D00)
private val Green = androidx.compose.ui.graphics.Color(0xFF007A33)
private val LightGreen = androidx.compose.ui.graphics.Color(0xFF66B3A1)
private val SoftGreen = androidx.compose.ui.graphics.Color(0xFFB2E0D4)
private val VeryLight = androidx.compose.ui.graphics.Color(0xFFE0F7F1)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database =
            NewayeDatabase.getDatabase(this)

        setContent {
            NewayeApp(database)
        }
    }
}

@Composable
fun NewayeApp(
    database: NewayeDatabase
) {

    var selectedTab by remember {
        mutableStateOf(0)
    }

    var showAccounts by remember {
        mutableStateOf(false)
    }

    var showTransactions by remember {
        mutableStateOf(false)
    }

    var showCategories by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(database) {
        val categoryDao = database.categoryDao()
        if (categoryDao.countCategories() == 0) {
            categoryDao.insertCategories(defaultCategories())
        }
    }

    if (showCategories) {
        CategoriesScreen(
            database = database,
            onBack = { showCategories = false }
        )
        return
    }

    if (showAccounts) {

        AccountsScreen(
            database = database,
            onBack = {
                showAccounts = false
            }
        )

        return
    }

    if (showTransactions) {

        TransactionsScreen(
            database = database,
            onBack = {
                showTransactions = false
            }
        )

        return
    }

    Scaffold(
        bottomBar = {

            Row(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement =
                    Arrangement.SpaceEvenly
            ) {

                TextButton(
                    onClick = {
                        selectedTab = 0
                    }
                ) {
                    Text("Home")
                }

                TextButton(
                    onClick = {
                        showTransactions = true
                    }
                ) {
                    Text("Transactions")
                }

                TextButton(
                    onClick = {
                        selectedTab = 1
                    }
                ) {
                    Text("Settings")
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            when (selectedTab) {

                0 -> Dashboard(
                    database = database,
                    onAccountsClick = {
                        showAccounts = true
                    },
                    onTransactionsClick = {
                        showTransactions = true
                    },
                    onCategoriesClick = {
                        showCategories = true
                    }
                )

                1 -> Settings()
            }
        }
    }
}

@Composable
fun Dashboard(
    database: NewayeDatabase,
    onAccountsClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onCategoriesClick: () -> Unit
) {

    val totalBalance by database
        .accountDao()
        .getTotalBalance()
        .collectAsStateWithLifecycle(
            initialValue = 0.0
        )

    // Observe transactions once so dashboard totals and recent activity
    // are calculated from the same reactive database snapshot.
    val transactions by database
        .transactionDao()
        .getAllTransactions()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val totalIncome = transactions
        .filter { it.type == "INCOME" }
        .sumOf { it.amount }

    val totalExpenses = transactions
        .filter { it.type == "EXPENSE" }
        .sumOf { it.amount }

    val monthRange = remember {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        start until calendar.timeInMillis
    }

    val currentMonthIncome = transactions
        .filter { it.type == "INCOME" && it.date in monthRange }
        .sumOf { it.amount }

    val currentMonthExpenses = transactions
        .filter { it.type == "EXPENSE" && it.date in monthRange }
        .sumOf { it.amount }

    val recentTransactions = transactions.take(5)

    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "ነዋዬ",
            color = DeepGreen,
            fontSize = 32.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Text(
            text = "Your Money. Your Control.",
            color = Green,
            fontWeight =
                androidx.compose.ui.text.font.FontWeight.Medium
        )

        Text(
            text = "Dashboard",
            fontSize = 25.sp,
            fontWeight =
                androidx.compose.ui.text.font.FontWeight.Bold
        )

        Card(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                24.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = DeepGreen
            )
        ) {

            Column(
                modifier = androidx.compose.ui.Modifier
                    .padding(22.dp)
            ) {

                Text(
                    "Total Balance",
                    color = androidx.compose.ui.graphics.Color.White
                        .copy(alpha = .8f)
                )

                Spacer(
                    modifier = androidx.compose.ui.Modifier
                        .height(8.dp)
                )

                Text(
                    "ETB %.2f".format(
                        totalBalance
                    ),
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 32.sp,
                    fontWeight =
                        androidx.compose.ui.text.font.FontWeight.Bold
                )

                Text(
                    if (totalBalance == 0.0) {
                        "No account balance yet"
                    } else {
                        "Balance across active accounts"
                    },
                    color = androidx.compose.ui.graphics.Color.White
                        .copy(alpha = .7f)
                )
            }
        }

        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            SmallCard(
                title = "Income",
                amount = "ETB %.2f".format(
                    totalIncome
                ),
                accent = Green,
                modifier = androidx.compose.ui.Modifier
                    .weight(1f)
            )

            SmallCard(
                title = "Expenses",
                amount = "ETB %.2f".format(
                    totalExpenses
                ),
                accent = LightGreen,
                modifier = androidx.compose.ui.Modifier
                    .weight(1f)
            )
        }

        Row(
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallCard(
                title = "Income this month",
                amount = "ETB %.2f".format(currentMonthIncome),
                accent = Green,
                modifier = androidx.compose.ui.Modifier.weight(1f)
            )
            SmallCard(
                title = "Expenses this month",
                amount = "ETB %.2f".format(currentMonthExpenses),
                accent = LightGreen,
                modifier = androidx.compose.ui.Modifier.weight(1f)
            )
        }

        Button(
            onClick = onTransactionsClick,
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                18.dp
            )
        ) {

            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null
            )

            Spacer(
                modifier = androidx.compose.ui.Modifier
                    .width(8.dp)
            )

            Text("Manage Transactions")
        }

        OutlinedButton(
            onClick = onAccountsClick,
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                18.dp
            )
        ) {

            Icon(
                imageVector =
                    Icons.Default.AccountBalanceWallet,
                contentDescription = null
            )

            Spacer(
                modifier = androidx.compose.ui.Modifier
                    .width(8.dp)
            )

            Text("Manage Accounts")
        }

        OutlinedButton(
            onClick = onCategoriesClick,
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
        ) {
            Text("Manage Categories")
        }

        Card(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = VeryLight
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                20.dp
            )
        ) {

            Column(
                modifier = androidx.compose.ui.Modifier
                    .padding(18.dp)
            ) {

                Text(
                    "Recent Transactions",
                    color = DeepGreen,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))

                if (recentTransactions.isEmpty()) {
                    Text(
                        "No transactions yet. Add your first transaction to see it here.",
                        color = androidx.compose.ui.graphics.Color.DarkGray
                    )
                } else {
                    recentTransactions.forEach { transaction ->
                        Row(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = androidx.compose.ui.Modifier.weight(1f)
                            ) {
                                Text(
                                    transaction.category.ifBlank { "Uncategorized" },
                                    color = DeepGreen,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                        .format(Date(transaction.date)),
                                    color = androidx.compose.ui.graphics.Color.DarkGray,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = (if (transaction.type == "INCOME") "+" else "−") +
                                    " ETB %.2f".format(transaction.amount),
                                color = if (transaction.type == "INCOME") Green else DeepGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Milestone 6 • Dashboard",
            color = LightGreen,
            modifier = androidx.compose.ui.Modifier
                .align(
                    androidx.compose.ui.Alignment.CenterHorizontally
                )
        )
    }
}

@Composable
fun TransactionsScreen(
    database: NewayeDatabase,
    onBack: () -> Unit
) {

    val repository = remember(database) {
        TransactionRepository(database)
    }

    val factory = remember(repository) {
        TransactionViewModelFactory(
            repository
        )
    }

    val transactionViewModel: TransactionViewModel =
        viewModel(
            factory = factory
        )

    val transactions by transactionViewModel
        .transactions
        .collectAsStateWithLifecycle()

    val accounts by database
        .accountDao()
        .getAllAccounts()
        .collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var editingTransaction by remember {
        mutableStateOf<TransactionEntity?>(null)
    }

    var deletingTransaction by remember {
        mutableStateOf<TransactionEntity?>(null)
    }

    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            TextButton(
                onClick = onBack
            ) {
                Text("← Back")
            }

            Text(
                "Transactions",
                style = MaterialTheme.typography
                    .headlineSmall
            )

            IconButton(
                onClick = {
                    showAddDialog = true
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription =
                        "Add transaction"
                )
            }
        }

        Spacer(
            modifier = androidx.compose.ui.Modifier
                .height(8.dp)
        )

        if (transactions.isEmpty()) {

            Card(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxWidth()
            ) {

                Column(
                    modifier = androidx.compose.ui.Modifier
                        .padding(24.dp),
                    horizontalAlignment =
                        androidx.compose.ui.Alignment.CenterHorizontally
                ) {

                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = Green
                    )

                    Spacer(
                        modifier = androidx.compose.ui.Modifier
                            .height(12.dp)
                    )

                    Text(
                        "No transactions yet",
                        fontWeight =
                            androidx.compose.ui.text.font.FontWeight.Bold
                    )

                    Text(
                        "Add your first income or expense."
                    )

                    Spacer(
                        modifier = androidx.compose.ui.Modifier
                            .height(12.dp)
                    )

                    Button(
                        onClick = {
                            showAddDialog = true
                        }
                    ) {
                        Text("Add Transaction")
                    }
                }
            }

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                items(
                    transactions,
                    key = {
                        it.id
                    }
                ) { transaction ->

                    val accountName =
                        accounts
                            .firstOrNull {
                                it.id ==
                                    transaction.accountId
                            }
                            ?.name
                            ?: "Unknown account"

                    TransactionCard(
                        transaction = transaction,
                        accountName = accountName,
                        onEdit = {
                            editingTransaction =
                                transaction
                        },
                        onDelete = {
                            deletingTransaction =
                                transaction
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {

        TransactionDialog(
            database = database,
            title = "Add Transaction",
            accounts = accounts,
            transaction = null,
            onDismiss = {
                showAddDialog = false
            },
            onSave = { transaction ->

                transactionViewModel
                    .addTransaction(transaction)

                showAddDialog = false
            }
        )
    }

    editingTransaction?.let { transaction ->

        TransactionDialog(
            database = database,
            title = "Edit Transaction",
            accounts = accounts,
            transaction = transaction,
            onDismiss = {
                editingTransaction = null
            },
            onSave = { updated ->

                transactionViewModel
                    .updateTransaction(updated)

                editingTransaction = null
            }
        )
    }

    deletingTransaction?.let { transaction ->

        AlertDialog(
            onDismissRequest = {
                deletingTransaction = null
            },
            title = {
                Text("Delete Transaction?")
            },
            text = {
                Text(
                    "This will remove the transaction and restore the affected account balance."
                )
            },
            confirmButton = {

                TextButton(
                    onClick = {

                        transactionViewModel
                            .deleteTransaction(
                                transaction
                            )

                        deletingTransaction = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {

                TextButton(
                    onClick = {
                        deletingTransaction = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    accountName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val isIncome =
        transaction.type == "INCOME"

    Card(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
    ) {

        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment =
                androidx.compose.ui.Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    if (isIncome) {
                        Icons.Default.ArrowUpward
                    } else {
                        Icons.Default.ArrowDownward
                    },
                contentDescription = null,
                tint =
                    if (isIncome) {
                        Green
                    } else {
                        DeepGreen
                    }
            )

            Spacer(
                modifier = androidx.compose.ui.Modifier
                    .width(12.dp)
            )

            Column(
                modifier = androidx.compose.ui.Modifier
                    .weight(1f)
            ) {

                Text(
                    transaction.category,
                    fontWeight =
                        androidx.compose.ui.text.font.FontWeight.Bold
                )

                Text(
                    accountName,
                    color = androidx.compose.ui.graphics.Color.Gray
                )

                if (transaction.note.isNotBlank()) {

                    Text(
                        transaction.note,
                        color =
                            androidx.compose.ui.graphics.Color.Gray
                    )
                }

                Text(
                    formatTransactionDate(
                        transaction.date
                    ),
                    color =
                        androidx.compose.ui.graphics.Color.Gray
                )
            }

            Column(
                horizontalAlignment =
                    androidx.compose.ui.Alignment.End
            ) {

                Text(
                    "${if (isIncome) "+" else "-"} ETB ${
                        "%.2f".format(
                            transaction.amount
                        )
                    }",
                    fontWeight =
                        androidx.compose.ui.text.font.FontWeight.Bold,
                    color =
                        if (isIncome) {
                            Green
                        } else {
                            DeepGreen
                        }
                )

                Row {

                    IconButton(
                        onClick = onEdit
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription =
                                "Edit"
                        )
                    }

                    IconButton(
                        onClick = onDelete
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription =
                                "Delete"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionDialog(
    database: NewayeDatabase,
    title: String,
    accounts: List<AccountEntity>,
    transaction: TransactionEntity?,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {

    var type by remember(
        transaction?.id
    ) {
        mutableStateOf(
            transaction?.type ?: "EXPENSE"
        )
    }

    var selectedAccount by remember(
        transaction?.id,
        accounts
    ) {
        mutableStateOf(
            accounts.firstOrNull {
                it.id ==
                    transaction?.accountId
            } ?: accounts.firstOrNull()
        )
    }

    var amountText by remember(
        transaction?.id
    ) {
        mutableStateOf(
            transaction?.amount?.toString() ?: ""
        )
    }

    var category by remember(
        transaction?.id
    ) {
        mutableStateOf(
            transaction?.category ?: ""
        )
    }

    var note by remember(
        transaction?.id
    ) {
        mutableStateOf(
            transaction?.note ?: ""
        )
    }

    var accountMenuExpanded by remember {
        mutableStateOf(false)
    }

    val categories by database.categoryDao()
        .getCategoriesByType(type)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var categoryMenuExpanded by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(title)
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedButton(
                        onClick = {
                            type = "INCOME"
                            category = ""
                        },
                        modifier =
                            androidx.compose.ui.Modifier
                                .weight(1f)
                    ) {
                        Text("Income")
                    }

                    OutlinedButton(
                        onClick = {
                            type = "EXPENSE"
                            category = ""
                        },
                        modifier =
                            androidx.compose.ui.Modifier
                                .weight(1f)
                    ) {
                        Text("Expense")
                    }
                }

                Text(
                    "Account",
                    fontWeight =
                        androidx.compose.ui.text.font.FontWeight.Bold
                )

                androidx.compose.material3.ExposedDropdownMenuBox(
                    expanded = accountMenuExpanded,
                    onExpandedChange = {
                        accountMenuExpanded =
                            !accountMenuExpanded
                    }
                ) {

                    OutlinedTextField(
                        value =
                            selectedAccount?.name
                                ?: "No account",
                        onValueChange = {},
                        readOnly = true,
                        modifier =
                            androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                        label = {
                            Text("Account")
                        }
                    )

                    DropdownMenu(
                        expanded = accountMenuExpanded,
                        onDismissRequest = {
                            accountMenuExpanded =
                                false
                        }
                    ) {

                        accounts.forEach { account ->

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${account.name} • ${account.type}"
                                    )
                                },
                                onClick = {

                                    selectedAccount =
                                        account

                                    accountMenuExpanded =
                                        false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                    },
                    label = {
                        Text("Amount (ETB)")
                    },
                    singleLine = true,
                    modifier =
                        androidx.compose.ui.Modifier
                            .fillMaxWidth()
                )

                androidx.compose.material3.ExposedDropdownMenuBox(
                    expanded = categoryMenuExpanded,
                    onExpandedChange = {
                        categoryMenuExpanded =
                            !categoryMenuExpanded
                    }
                ) {

                    OutlinedTextField(
                        value = category,
                        onValueChange = {
                            category = it
                        },
                        label = {
                            Text("Category")
                        },
                        modifier =
                            androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                    )

                    DropdownMenu(
                        expanded =
                            categoryMenuExpanded,
                        onDismissRequest = {
                            categoryMenuExpanded =
                                false
                        }
                    ) {

                        categories.forEach { item ->

                            DropdownMenuItem(
                                text = {
                                    Text(item.name)
                                },
                                onClick = {

                                    category = item.name

                                    categoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        note = it
                    },
                    label = {
                        Text("Note")
                    },
                    modifier =
                        androidx.compose.ui.Modifier
                            .fillMaxWidth(),
                    minLines = 2
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    val amount =
                        amountText
                            .toDoubleOrNull()

                    if (
                        amount != null &&
                        amount > 0 &&
                        selectedAccount != null &&
                        category.isNotBlank()
                    ) {

                        onSave(
                            TransactionEntity(
                                id =
                                    transaction?.id
                                        ?: 0,

                                accountId =
                                    selectedAccount!!.id,

                                type = type,

                                amount =
                                    abs(amount),

                                category =
                                    category.trim(),

                                note =
                                    note.trim(),

                                date =
                                    transaction?.date
                                        ?: System.currentTimeMillis(),

                                currency = "ETB"
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}


private fun defaultCategories(): List<CategoryEntity> = listOf(
    CategoryEntity(name = "Salary", type = "INCOME", isDefault = true),
    CategoryEntity(name = "Business Income", type = "INCOME", isDefault = true),
    CategoryEntity(name = "Gift", type = "INCOME", isDefault = true),
    CategoryEntity(name = "Interest", type = "INCOME", isDefault = true),
    CategoryEntity(name = "Other Income", type = "INCOME", isDefault = true),
    CategoryEntity(name = "Food", type = "EXPENSE", isDefault = true),
    CategoryEntity(name = "Transport", type = "EXPENSE", isDefault = true),
    CategoryEntity(name = "Rent", type = "EXPENSE", isDefault = true),
    CategoryEntity(name = "Utilities", type = "EXPENSE", isDefault = true),
    CategoryEntity(name = "Shopping", type = "EXPENSE", isDefault = true),
    CategoryEntity(name = "Education", type = "EXPENSE", isDefault = true),
    CategoryEntity(name = "Health", type = "EXPENSE", isDefault = true),
    CategoryEntity(name = "Other Expense", type = "EXPENSE", isDefault = true)
)

@Composable
fun CategoriesScreen(
    database: NewayeDatabase,
    onBack: () -> Unit
) {
    val categoryDao = database.categoryDao()
    val categories by categoryDao.getAllCategories()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    var showEditor by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryName by remember { mutableStateOf("") }
    var categoryType by remember { mutableStateOf("EXPENSE") }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var deletingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var deleteMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("← Back") }
            Text("Categories", style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = {
                editingCategory = null
                categoryName = ""
                categoryType = "EXPENSE"
                validationMessage = null
                showEditor = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        }

        Text(
            "Organize income and expenses with categories. Changes are saved on this device.",
            color = androidx.compose.ui.graphics.Color.DarkGray
        )

        listOf("INCOME", "EXPENSE").forEach { groupType ->
            Text(
                if (groupType == "INCOME") "Income categories" else "Expense categories",
                style = MaterialTheme.typography.titleMedium,
                color = DeepGreen
            )
            val group = categories.filter { it.type == groupType }
            if (group.isEmpty()) {
                Text("No categories yet.")
            } else {
                group.forEach { item ->
                    Card(modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
                        Row(
                            modifier = androidx.compose.ui.Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Medium)
                                Text(
                                    if (item.isDefault) "Default category" else "Custom category",
                                    fontSize = 12.sp,
                                    color = androidx.compose.ui.graphics.Color.Gray
                                )
                            }
                            IconButton(onClick = {
                                editingCategory = item
                                categoryName = item.name
                                categoryType = item.type
                                validationMessage = null
                                showEditor = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename ${item.name}")
                            }
                            if (!item.isDefault) {
                                IconButton(onClick = {
                                    scope.launch {
                                        val used = database.transactionDao()
                                            .countTransactionsWithCategory(item.name, item.type)
                                        if (used > 0) {
                                            deleteMessage = "This category is used by $used transaction(s). Rename it instead, or reassign those transactions before deleting."
                                        } else {
                                            deletingCategory = item
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete ${item.name}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        AlertDialog(
            onDismissRequest = { showEditor = false },
            title = { Text(if (editingCategory == null) "Add Category" else "Rename Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it; validationMessage = null },
                        label = { Text("Category name") },
                        singleLine = true
                    )
                    if (editingCategory == null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { categoryType = "INCOME" },
                                modifier = androidx.compose.ui.Modifier.weight(1f)
                            ) { Text(if (categoryType == "INCOME") "✓ Income" else "Income") }
                            OutlinedButton(
                                onClick = { categoryType = "EXPENSE" },
                                modifier = androidx.compose.ui.Modifier.weight(1f)
                            ) { Text(if (categoryType == "EXPENSE") "✓ Expense" else "Expense") }
                        }
                    } else {
                        Text("Type: ${if (categoryType == "INCOME") "Income" else "Expense"}")
                    }
                    validationMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color.Red) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cleaned = categoryName.trim()
                    val duplicate = categories.any {
                        it.type == categoryType &&
                            it.name.equals(cleaned, ignoreCase = true) &&
                            it.id != editingCategory?.id
                    }
                    when {
                        cleaned.isBlank() -> validationMessage = "Enter a category name."
                        cleaned.length > 40 -> validationMessage = "Use 40 characters or fewer."
                        duplicate -> validationMessage = "A category with this name already exists for this type."
                        else -> {
                            scope.launch {
                                val old = editingCategory
                                if (old == null) {
                                    categoryDao.insertCategory(
                                        CategoryEntity(name = cleaned, type = categoryType)
                                    )
                                } else {
                                    categoryDao.updateCategory(old.copy(name = cleaned))
                                    if (old.name != cleaned) {
                                        database.transactionDao().renameTransactionCategory(
                                            oldName = old.name,
                                            newName = cleaned,
                                            type = old.type
                                        )
                                    }
                                }
                                showEditor = false
                            }
                        }
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditor = false }) { Text("Cancel") }
            }
        )
    }

    deletingCategory?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text("Delete category?") },
            text = { Text("Delete '${item.name}'? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        categoryDao.deleteCategory(item)
                        deletingCategory = null
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) { Text("Cancel") }
            }
        )
    }

    deleteMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { deleteMessage = null },
            title = { Text("Category is in use") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { deleteMessage = null }) { Text("OK") }
            }
        )
    }
}

@Composable
fun AccountsScreen(
    database: NewayeDatabase,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val accounts by database
        .accountDao()
        .getAllAccounts()
        .collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

    var showDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            TextButton(
                onClick = onBack
            ) {
                Text("← Back")
            }

            Text(
                "Accounts",
                style = MaterialTheme.typography
                    .headlineSmall
            )

            IconButton(
                onClick = {
                    showDialog = true
                }
            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription =
                        "Add account"
                )
            }
        }

        Spacer(
            modifier = androidx.compose.ui.Modifier
                .height(12.dp)
        )

        if (accounts.isEmpty()) {

            Text("No accounts yet.")

        } else {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                items(
                    accounts,
                    key = {
                        it.id
                    }
                ) { account ->

                    AccountCard(
                        account
                    )
                }
            }
        }
    }

    if (showDialog) {

        AddAccountDialog(
            onDismiss = {
                showDialog = false
            },
            onSave = { account ->

    scope.launch {
        database
            .accountDao()
            .insertAccount(
                account
            )
    }

    showDialog = false
}
        )
    }
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var type by remember {
        mutableStateOf("Cash")
    }

    var balance by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Add Account")
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    label = {
                        Text("Account Name")
                    },
                    modifier =
                        androidx.compose.ui.Modifier
                            .fillMaxWidth()
                )

                OutlinedTextField(
                    value = type,
                    onValueChange = {
                        type = it
                    },
                    label = {
                        Text("Type")
                    },
                    modifier =
                        androidx.compose.ui.Modifier
                            .fillMaxWidth()
                )

                OutlinedTextField(
                    value = balance,
                    onValueChange = {
                        balance = it
                    },
                    label = {
                        Text("Opening Balance (ETB)")
                    },
                    modifier =
                        androidx.compose.ui.Modifier
                            .fillMaxWidth()
                )
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    val openingBalance =
                        balance
                            .toDoubleOrNull()
                            ?: 0.0

                    if (name.isNotBlank()) {

                        onSave(
                            AccountEntity(
                                name =
                                    name.trim(),

                                type =
                                    type.trim(),

                                balance =
                                    openingBalance
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AccountCard(
    account: AccountEntity
) {

    Card(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
    ) {

        Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment =
                androidx.compose.ui.Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    when (account.type) {
                        "Bank" ->
                            Icons.Default.AccountBalance

                        "Mobile Wallet" ->
                            Icons.Default.Wallet

                        else ->
                            Icons.Default.AccountBalanceWallet
                    },
                contentDescription = null,
                tint = Green
            )

            Spacer(
                modifier = androidx.compose.ui.Modifier
                    .width(12.dp)
            )

            Column(
                modifier =
                    androidx.compose.ui.Modifier
                        .weight(1f)
            ) {

                Text(
                    account.name,
                    fontWeight =
                        androidx.compose.ui.text.font.FontWeight.Bold
                )

                Text(
                    account.type,
                    color =
                        androidx.compose.ui.graphics.Color.Gray
                )
            }

            Text(
                "ETB %.2f".format(
                    account.balance
                ),
                fontWeight =
                    androidx.compose.ui.text.font.FontWeight.Bold,
                color = DeepGreen
            )
        }
    }
}

@Composable
fun SmallCard(
    title: String,
    amount: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: androidx.compose.ui.Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = VeryLight
        ),
        shape =
            androidx.compose.foundation.shape.RoundedCornerShape(
                18.dp
            )
    ) {

        Column(
            modifier =
                androidx.compose.ui.Modifier
                    .padding(16.dp)
        ) {

            Text(
                title,
                color = accent,
                fontWeight =
                    androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(
                modifier =
                    androidx.compose.ui.Modifier
                        .height(6.dp)
            )

            Text(
                amount,
                fontWeight =
                    androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

@Composable
fun Settings() {

    Column(
        modifier =
            androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(20.dp)
    ) {

        Text(
            "Settings",
            style = MaterialTheme.typography
                .headlineMedium
        )

        Spacer(
            modifier =
                androidx.compose.ui.Modifier
                    .height(16.dp)
        )

        Text(
            "Language: English"
        )

        Spacer(
            modifier =
                androidx.compose.ui.Modifier
                    .height(8.dp)
        )

        Text(
            "Currency: ETB"
        )

        Spacer(
            modifier =
                androidx.compose.ui.Modifier
                    .height(8.dp)
        )

        Text(
            "Newaye Finance"
        )
    }
}

fun formatTransactionDate(
    timestamp: Long
): String {

    return SimpleDateFormat(
        "MMM dd, yyyy HH:mm",
        Locale.getDefault()
    ).format(
        Date(timestamp)
    )
}
