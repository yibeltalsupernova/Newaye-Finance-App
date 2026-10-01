package com.newaye.finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.newaye.finance.data.local.NewayeDatabase
import com.newaye.finance.data.local.entity.AccountEntity
import kotlinx.coroutines.launch

private val DeepGreen = Color(0xFF004D00)
private val Green = Color(0xFF007A33)
private val LightGreen = Color(0xFF66B3A1)
private val VeryLight = Color(0xFFE0F7F1)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = NewayeDatabase.getDatabase(this)

        setContent {
            NewayeApp(database)
        }
    }
}

@Composable
fun NewayeApp(database: NewayeDatabase) {

    var tab by remember {
        mutableIntStateOf(0)
    }

    var showAccounts by remember {
        mutableStateOf(false)
    }

    val items = listOf(
        "Home" to Icons.Default.Home,
        "Analytics" to Icons.Default.Analytics,
        "Budget" to Icons.Default.AccountBalanceWallet,
        "Settings" to Icons.Default.Settings
    )

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = DeepGreen,
            secondary = Green,
            tertiary = LightGreen,
            background = VeryLight
        )
    ) {

        if (showAccounts) {

            AccountsScreen(
                database = database,
                onBack = {
                    showAccounts = false
                }
            )

        } else {

            Scaffold(

                bottomBar = {

                    NavigationBar {

                        items.forEachIndexed { index, item ->

                            NavigationBarItem(
                                selected = tab == index,

                                onClick = {
                                    tab = index
                                },

                                icon = {
                                    Icon(
                                        imageVector = item.second,
                                        contentDescription = item.first
                                    )
                                },

                                label = {
                                    Text(item.first)
                                }
                            )
                        }
                    }
                }

            ) { padding ->

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {

                    when (tab) {

                        0 -> Dashboard(
                            database = database,
                            onAccountsClick = {
                                showAccounts = true
                            }
                        )

                        1 -> Placeholder(
                            "Analytics",
                            "Your financial insights will appear here."
                        )

                        2 -> Placeholder(
                            "Budget",
                            "Create and manage budgets here."
                        )

                        3 -> Settings()
                    }
                }
            }
        }
    }
}

@Composable
fun Dashboard(
    database: NewayeDatabase,
    onAccountsClick: () -> Unit
) {

    val totalBalance by database
        .accountDao()
        .getTotalBalance()
        .collectAsStateWithLifecycle(
            initialValue = 0.0
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "ነዋዬ",
            color = DeepGreen,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Your Money. Your Control.",
            color = Green,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Dashboard",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = DeepGreen
            )
        ) {

            Column(
                modifier = Modifier.padding(22.dp)
            ) {

                Text(
                    "Total Balance",
                    color = Color.White.copy(alpha = .8f)
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    "ETB %.2f".format(totalBalance),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    if (totalBalance == 0.0) {
                        "No account balance yet"
                    } else {
                        "Balance across active accounts"
                    },
                    color = Color.White.copy(alpha = .7f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            SmallCard(
                title = "Income",
                amount = "ETB 0.00",
                accent = Green,
                modifier = Modifier.weight(1f)
            )

            SmallCard(
                title = "Expenses",
                amount = "ETB 0.00",
                accent = LightGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = onAccountsClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {

            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text("Manage Accounts")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = VeryLight
            ),
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(
                modifier = Modifier.padding(18.dp)
            ) {

                Text(
                    "Recent Transactions",
                    color = DeepGreen,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    "No transactions yet",
                    color = Color.DarkGray
                )
            }
        }

        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Milestone 2 • Accounts",
            color = LightGreen,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )
    }
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

    var showAddAccount by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Accounts",
                color = DeepGreen,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        if (accounts.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                Icon(
                    imageVector =
                        Icons.Default.AccountBalanceWallet,

                    contentDescription = null,

                    tint = LightGreen,

                    modifier = Modifier.size(64.dp)
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    "No accounts yet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Add your first money account."
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = accounts,
                    key = { it.id }
                ) { account ->

                    AccountCard(account)
                }
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = {
                showAddAccount = true
            },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(18.dp)
        ) {

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text("Add Account")
        }
    }

    if (showAddAccount) {

        AddAccountDialog(
            database = database,

            onDismiss = {
                showAddAccount = false
            }
        )
    }
}

@Composable
fun AccountCard(
    account: AccountEntity
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector = when (account.type) {

                    "Cash" ->
                        Icons.Default.Payments

                    "Bank" ->
                        Icons.Default.AccountBalance

                    "Mobile Wallet" ->
                        Icons.Default.PhoneAndroid

                    else ->
                        Icons.Default.AccountBalanceWallet
                },

                contentDescription = null,

                tint = Green,

                modifier = Modifier.size(36.dp)
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    account.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    account.type,
                    color = Color.Gray
                )
            }

            Text(
                "ETB %.2f".format(account.balance),
                fontWeight = FontWeight.Bold,
                color = DeepGreen
            )
        }
    }
}

@Composable
fun AddAccountDialog(
    database: NewayeDatabase,
    onDismiss: () -> Unit
) {

    val scope = rememberCoroutineScope()

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
                    Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = name,

                    onValueChange = {
                        name = it
                    },

                    label = {
                        Text("Account name")
                    },

                    singleLine = true
                )

                Text(
                    "Account type",
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    listOf(
                        "Cash",
                        "Bank",
                        "Mobile Wallet"
                    ).forEach { option ->

                        FilterChip(
                            selected =
                                type == option,

                            onClick = {
                                type = option
                            },

                            label = {
                                Text(option)
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = balance,

                    onValueChange = {
                        balance = it
                    },

                    label = {
                        Text("Opening balance")
                    },

                    singleLine = true
                )
            }
        },

        confirmButton = {

            TextButton(

                enabled =
                    name.isNotBlank(),

                onClick = {

                    val amount =
                        balance.toDoubleOrNull()
                            ?: 0.0

                    val account =
                        AccountEntity(
                            name = name.trim(),
                            type = type,
                            balance = amount
                        )

                    scope.launch {

                        database
                            .accountDao()
                            .insertAccount(account)

                        onDismiss()
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
fun SmallCard(
    title: String,
    amount: String,
    accent: Color,
    modifier: Modifier
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                title,
                color = accent,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                amount,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun Placeholder(
    title: String,
    message: String
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            title,
            color = DeepGreen,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            message,
            color = Color.Gray
        )
    }
}

@Composable
fun Settings() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            "Settings",
            color = DeepGreen,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        Text(
            "Language",
            fontWeight = FontWeight.Bold
        )

        Text(
            "English • Amharic foundation ready"
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Text(
            "Currency",
            fontWeight = FontWeight.Bold
        )

        Text(
            "ETB — Ethiopian Birr"
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Text(
            "Appearance",
            fontWeight = FontWeight.Bold
        )

        Text(
            "System theme"
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            "ነዋዬ • ገንዘብዎ ፣በእጅዎ",
            color = Green,
            fontWeight = FontWeight.Bold
        )
    }
}
