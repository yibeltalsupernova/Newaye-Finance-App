package com.newaye.finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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

private val DeepGreen = Color(0xFF004D00)
private val Green = Color(0xFF007A33)
private val LightGreen = Color(0xFF66B3A1)
private val VeryLight = Color(0xFFE0F7F1)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NewayeApp()
        }
    }
}

@Composable
fun NewayeApp() {

    var tab by remember {
        mutableIntStateOf(0)
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

                    0 -> Dashboard()

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

@Composable
fun Dashboard() {

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
                    "ETB 0.00",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "No accounts added yet",
                    color = Color.White.copy(alpha = .7f)
                )
            }
        }

        Row(

            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(12.dp)

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
            text = "Milestone 1 • Foundation",
            color = LightGreen,
            modifier = Modifier.align(
                Alignment.CenterHorizontally
            )
        )
    }
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
