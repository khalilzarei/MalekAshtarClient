package com.khz.malekashtarclient.domain.model

/**
 * اطلاعات مالی بازیکن (از me/finance)
 */
data class MyFinance(
    val playerId: Int,
    val playerName: String,
    val totalInvoiced: Long,
    val totalPaid: Long,
    val balance: Long,
    val pendingPayments: Int,
    val pendingAmount: Long,
    val invoices: List<MyInvoice>
)

/** یک فاکتور */
data class MyInvoice(
    val id: Int,
    val invoiceNumber: String?,
    val invoiceType: String?,       // monthly | ...
    val periodStartDate: String?,   // YYYY-MM-DD
    val periodEndDate: String?,
    val dueDate: String?,
    val status: String?,            // open | partial | paid | ...
    val totalAmount: Long,
    val paidAmount: Long,
    val remainingAmount: Long
)
