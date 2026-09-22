package com.khz.malekashtarclient.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import com.khz.malekashtarclient.domain.model.ClassDebt
import com.khz.malekashtarclient.domain.model.ClassFee
import com.khz.malekashtarclient.domain.model.InvoiceItem
import com.khz.malekashtarclient.domain.model.MyFinance
import com.khz.malekashtarclient.domain.model.MyInvoice
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.RedError

@Composable
fun FinanceScreen(onBack: () -> Unit) {
    val viewModel: FinanceViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            GlassTopBar(title = "صورت حساب", onBack = onBack)

            when (state) {
                is com.khz.malekashtarclient.ui.components.ListState.Loading -> {
                    com.khz.malekashtarclient.ui.components.LoadingContent(modifier = Modifier.padding(top = 56.dp))
                }
                is com.khz.malekashtarclient.ui.components.ListState.Error -> {
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
                        val finance = finances.first()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(top = 64.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item { FinanceSummaryCard(finance) }

                            // بدهکار بودن
                            if (finance.isDebtor) {
                                item {
                                    GlassCard3D {
                                        Row(
                                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(RedError.copy(0.12f)).border(0.5.dp, RedError.copy(0.3f), RoundedCornerShape(12.dp)).padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Warning, null, tint = RedError, modifier = Modifier.size(22.dp))
                                            Spacer(Modifier.width(10.dp))
                                            Column {
                                                Text("شما بدهکار هستید", color = RedError, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                                Text("مبلغ بدهی: ${finance.debt.toPersianDigits()} تومان - لطفاً نسبت به پرداخت اقدام کنید", color = Color.White.copy(0.8f), style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }

                            // شهریه کلاس‌ها (مصوب ادمین)
                            if (finance.classFees.isNotEmpty()) {
                                item {
                                    Text("شهریه کلاس‌ها", color = GoldPrimary, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 8.dp))
                                }
                                items(finance.classFees, key = { it.classId ?: it.classTitle.hashCode() }) { fee ->
                                    ClassFeeCard(fee)
                                }
                            }

                            // بدهی به تفکیک کلاس
                            if (finance.classDebts.isNotEmpty()) {
                                item {
                                    Text("بدهی به تفکیک کلاس", color = GoldPrimary, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 8.dp))
                                }
                                items(finance.classDebts, key = { (it.classId ?: 0).toString() + it.classTitle }) { debt ->
                                    ClassDebtCard(debt)
                                }
                            }

                            item {
                                Text("فاکتورها (${finance.invoices.size.toPersianDigits()})", color = GoldPrimary, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 8.dp))
                            }
                            items(finance.invoices, key = { it.id }) { inv ->
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalanceWallet, null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = f.playerName, color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(Modifier.weight(1f))
                if (f.isDebtor) {
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(RedError.copy(0.2f)).border(0.5.dp, RedError.copy(0.4f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("بدهکار", color = RedError, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                } else {
                    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF4CAF50).copy(0.2f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("تسویه", color = Color(0xFF81C784), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem("کل فاکتور", "${f.totalInvoiced.toPersianDigits()} ت", Modifier.weight(1f))
                SummaryItem("پرداخت‌شده", "${f.totalPaid.toPersianDigits()} ت", Modifier.weight(1f), color = Color(0xFF81C784))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem("بدهی کل", "${f.debt.toPersianDigits()} ت", Modifier.weight(1f), color = if (f.debt > 0) RedError else Color.White)
                SummaryItem("در انتظار", "${f.pendingPayments.toPersianDigits()} مورد", Modifier.weight(1f), color = GoldPrimary)
            }
            if (f.pendingAmount > 0) {
                Text("مبلغ در انتظار تایید: ${f.pendingAmount.toPersianDigits()} ت", color = GoldPrimary.copy(0.8f), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ClassFeeCard(fee: ClassFee) {
    GlassCard3D {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Class, null, tint = GoldPrimary.copy(0.8f), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(fee.classTitle, color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                }
                if (fee.debt > 0) {
                    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(RedError.copy(0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("${fee.debt.toPersianDigits()} ت بدهی", color = RedError, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                } else {
                    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF4CAF50).copy(0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("تسویه", color = Color(0xFF81C784), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            fee.ageGroupTitle?.let { Text(it, color = Color.White.copy(0.6f), style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth()) {
                fee.monthlyFee?.let { AmountItem("شهریه ماهانه", "${it.toPersianDigits()} ت", Modifier.weight(1f)) }
                fee.sessionFee?.let { AmountItem("هر جلسه", "${it.toPersianDigits()} ت", Modifier.weight(1f)) }
                fee.registrationFee?.let { AmountItem("ثبت‌نام", "${it.toPersianDigits()} ت", Modifier.weight(1f)) }
            }
            Row(Modifier.fillMaxWidth()) {
                AmountItem("کل فاکتور", "${fee.total.toPersianDigits()} ت", Modifier.weight(1f))
                AmountItem("پرداخت‌شده", "${fee.paid.toPersianDigits()} ت", Modifier.weight(1f), color = Color(0xFF81C784))
                AmountItem("مانده", "${fee.debt.toPersianDigits()} ت", Modifier.weight(1f), color = if (fee.debt > 0) RedError else Color.White)
            }
        }
    }
}

@Composable
private fun ClassDebtCard(debt: ClassDebt) {
    GlassCard3D {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(debt.classTitle, color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                debt.ageGroupTitle?.let { Text(it, color = Color.White.copy(0.5f), style = MaterialTheme.typography.labelSmall) }
                Text("${debt.itemsCount.toPersianDigits()} آیتم", color = Color.White.copy(0.4f), style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${debt.total.toPersianDigits()} ت", color = Color.White.copy(0.7f), style = MaterialTheme.typography.bodySmall)
                Text("پرداخت: ${debt.paid.toPersianDigits()} ت", color = Color(0xFF81C784), style = MaterialTheme.typography.labelSmall)
                Text("بدهی: ${debt.remaining.toPersianDigits()} ت", color = if (debt.remaining > 0) RedError else Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, modifier: Modifier = Modifier, color: Color = Color.White) {
    Column(modifier = modifier) {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        Text(text = value.toPersianDigits(), color = color, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun InvoiceCard(inv: MyInvoice) {
    GlassCard3D {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("فاکتور #${inv.invoiceNumber?.toPersianDigits() ?: inv.id.toString().toPersianDigits()}", color = Color.White, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                StatusBadge(inv.status)
            }
            if (!inv.periodStartDate.isNullOrBlank() && !inv.periodEndDate.isNullOrBlank()) {
                Text("دوره: ${DateUtils.toJalaliReadable(inv.periodStartDate)} تا ${DateUtils.toJalaliReadable(inv.periodEndDate)}".toPersianDigits(), color = Color.White.copy(0.7f), style = MaterialTheme.typography.bodySmall)
            }
            if (!inv.dueDate.isNullOrBlank()) {
                Text("سررسید: ${DateUtils.toJalaliReadable(inv.dueDate).toPersianDigits()}", color = Color.White.copy(0.7f), style = MaterialTheme.typography.bodySmall)
            }
            inv.notes?.takeIf { it.isNotBlank() }?.let { Text("یادداشت: $it", color = Color.White.copy(0.6f), style = MaterialTheme.typography.bodySmall) }

            if (inv.items.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White.copy(0.04f)).padding(10.dp)) {
                    Text("آیتم‌ها:", color = GoldPrimary.copy(0.9f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    inv.items.forEach { item -> InvoiceItemRow(item) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                AmountItem("مبلغ", "${inv.totalAmount.toPersianDigits()} ت", Modifier.weight(1f))
                AmountItem("پرداخت‌شده", "${inv.paidAmount.toPersianDigits()} ت", Modifier.weight(1f), color = Color(0xFF81C784))
                AmountItem("مانده", "${inv.remainingAmount.toPersianDigits()} ت", Modifier.weight(1f), color = if (inv.remainingAmount > 0) RedError else Color.White)
            }
            if (inv.discountTotal > 0) {
                Text("تخفیف: ${inv.discountTotal.toPersianDigits()} ت", color = Color(0xFF81C784), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun InvoiceItemRow(item: InvoiceItem) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title.toPersianDigits(), color = Color.White.copy(0.85f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item.classTitle?.let { Text(it, color = Color.White.copy(0.5f), style = MaterialTheme.typography.labelSmall) }
                item.ageGroupTitle?.let { Text("· $it", color = Color.White.copy(0.4f), style = MaterialTheme.typography.labelSmall) }
            }
        }
        Text("${item.total.toPersianDigits()} ت", color = GoldPrimary, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun AmountItem(label: String, value: String, modifier: Modifier = Modifier, color: Color = Color.White) {
    Column(modifier = modifier) {
        Text(text = label, color = Color.White.copy(alpha = 0.5f), style = MaterialTheme.typography.labelSmall)
        Text(text = value.toPersianDigits(), color = color, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val (text, color) = when (status) {
        "paid" -> "پرداخت‌شده" to Color(0xFF81C784)
        "partial" -> "پرداخت جزئی" to GoldPrimary
        "open" -> "پرداخت‌نشده" to RedError
        else -> (status ?: "نامشخص") to Color.White.copy(0.6f)
    }
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.25f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text = text, color = color, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(top = 56.dp), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.White.copy(0.6f), style = MaterialTheme.typography.bodyMedium)
    }
}
