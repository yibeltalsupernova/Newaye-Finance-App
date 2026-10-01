package com.newaye.finance
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.newaye.finance.data.local.NewayeDatabase
import com.newaye.finance.data.local.entity.AccountEntity
import com.newaye.finance.data.local.entity.TransactionEntity
import com.newaye.finance.data.repository.TransactionRepository
import com.newaye.finance.ui.TransactionViewModel
import com.newaye.finance.ui.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    onTransactionsClick: () -> Unit
) {

    val totalBalance by database
        .accountDao()
        .getTotalBalance()
        .collectAsStateWithLifecycle(
            initialValue = 0.0
        )

    val totalIncome by database
        .transactionDao()
        .getTotalIncome()
        .collectAsStateWithLifecycle(
            initialValue = 0.0
        )

    val totalExpenses by database
        .transactionDao()
        .getTotalExpenses()
        .collectAsStateWithLifecycle(
            initialValue = 0.0
        )

    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
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
                    fontWeight =
                        androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(
                    modifier = androidx.compose.ui.Modifier
                        .height(8.dp)
                )

                Text(
                    "Open Manage Transactions to view your transaction history.",
                    color = androidx.compose.ui.graphics.Color.DarkGray
                )
            }
        }

        Spacer(
            modifier = androidx.compose.ui.Modifier
                .weight(1f)
        )

        Text(
            text = "Milestone 3 • Transactions",
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

    val categories =
        if (type == "INCOME") {

            listOf(
                "Salary",
                "Business",
                "Gift",
                "Interest",
                "Other Income"
            )

        } else {

            listOf(
                "Food",
                "Transport",
                "Rent",
                "Utilities",
                "Shopping",
                "Education",
                "Health",
                "Other Expense"
            )
        }

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
                                    Text(item)
                                },
                                onClick = {

                                    category =
                                        item

                                    categoryMenuExpanded =
                                        false
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

@Composable
fun AccountsScreen(
    database: NewayeDatabase,
    onBack: () -> Unit
) {

    val accounts by database
        .accountDao()
        .getAllAccounts()
        .collectAsStateWithLifecycle(
            initialValue = emptyList()
        )

    var showDialog by remember {
        mutableStateOf(false)
    }
val scope = rememberCoroutineScope()
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

                androidx.compose.runtime.rememberCoroutineScope()
                    .launch {
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
