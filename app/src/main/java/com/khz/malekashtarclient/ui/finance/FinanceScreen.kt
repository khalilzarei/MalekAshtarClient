package com.khz.malekashtarclient.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.core.util.DateUtils
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.MyFinance
import com.khz.malekashtarclient.domain.model.MyInvoice
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.RedError

/**
 * صفحه‌ی صورت حساب
 */
@Composable
fun FinanceScreen(onBack: () -> Unit) {
    val viewModel: FinanceViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = "صورت حساب",
                onBack = onBack
            )

            when (state) {
                is com.khz.malekashtarclient.ui.components.ListState.Loading -> {
                    com.khz.malekashtarclient.ui.components.LoadingContent(
                        modifier = Modifier.padding(top = 56.dp)
                    )
                }

                is com.khz.malekashtarclient.ui.components.ListState.Error   -> {
                    com.khz.malekashtarclient.ui.components.ErrorContent(
                        message = (state as com.khz.malekashtarclient.ui.components.ListState.Error).message,
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.padding(top = 56.dp)
                    )
                }

                is com.khz.malekashtarclient.ui.components.ListState.Success -> {
                    val finances = (state as com.khz.malekashtarclient.ui.components.ListState.Success<MyFinance>).items
                    if (finances.isEmpty()) {
                        EmptyState("موردی برای نمایش وجود ندارد")
                    } else {
                        // فقط بازیکن اول (معمولاً ۱)
                        val finance = finances.first()
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 64.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { FinanceSummaryCard(finance) }
                            item {
                                Text(
                                    text = "فاکتورها (${finance.invoices.size.toPersianDigits()})",
                                    color = GoldPrimary,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(
                                finance.invoices,
                                key = { it.id },
                            ) { inv ->
                                InvoiceCard(inv)
                            }

                            item { Spacer(Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceSummaryCard(f: MyFinance) {
    GlassCard3D {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = f.playerName,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem(
                    "کل فاکتور",
                    "${f.totalInvoiced.toPersianDigits()} ت",
                    Modifier.weight(1f)
                )
                SummaryItem(
                    "پرداخت‌شده",
                    "${f.totalPaid.toPersianDigits()} ت",
                    Modifier.weight(1f),
                    color = Color(0xFF81C784)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem(
                    "مانده",
                    "${f.balance.toPersianDigits()} ت",
                    Modifier.weight(1f),
                    color = if (f.balance > 0) RedError else Color.White
                )
                SummaryItem(
                    "در انتظار",
                    "${f.pendingPayments.toPersianDigits()} مورد",
                    Modifier.weight(1f),
                    color = GoldPrimary
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = value,
            color = color,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun InvoiceCard(inv: MyInvoice) {
    GlassCard3D {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "فاکتور #${
                        inv.invoiceNumber?.toPersianDigits() ?: inv.id.toString()
                            .toPersianDigits()
                    }",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                StatusBadge(inv.status)
            }

            Spacer(Modifier.height(8.dp))

            if (!inv.periodStartDate.isNullOrBlank() && !inv.periodEndDate.isNullOrBlank()) {
                Text(
                    text = "دوره: ${DateUtils.toJalaliReadable(inv.periodStartDate)} تا ${DateUtils.toJalaliReadable(inv.periodEndDate)}",
                    color = Color.White.copy(0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!inv.dueDate.isNullOrBlank()) {
                Text(
                    text = "سررسید: ${DateUtils.toJalaliReadable(inv.dueDate)}",
                    color = Color.White.copy(0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                AmountItem(
                    "مبلغ",
                    "${inv.totalAmount.toPersianDigits()} ت",
                    Modifier.weight(1f)
                )
                AmountItem(
                    "پرداخت‌شده",
                    "${inv.paidAmount.toPersianDigits()} ت",
                    Modifier.weight(1f),
                    color = Color(0xFF81C784)
                )
                AmountItem(
                    "مانده",
                    "${inv.remainingAmount.toPersianDigits()} ت",
                    Modifier.weight(1f),
                    color = if (inv.remainingAmount > 0) RedError else Color.White
                )
            }
        }
    }
}

@Composable
private fun AmountItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = value,
            color = color,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val (text, color) = when (status) {
        "paid"    -> "پرداخت‌شده" to Color(0xFF81C784)
        "partial" -> "پرداخت جزئی" to GoldPrimary
        "open"    -> "پرداخت‌نشده" to RedError
        else      -> (status
                ?: "نامشخص") to Color.White.copy(0.6f)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.25f))
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp
            )
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.White.copy(0.6f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
