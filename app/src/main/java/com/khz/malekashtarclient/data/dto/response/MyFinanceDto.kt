package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

/**
 * پاسخ GET me/finance → data.finance[]
 *
 * سرور draft/cancelled فاکتورها را نمی‌فرستد.
 * مبالغ به تومان هستند.
 */
data class MyFinanceDto(
    @SerializedName("player_id") val playerId: Int? = null,
    @SerializedName("player_name") val playerName: String? = null,
    @SerializedName("total_invoiced") val totalInvoiced: Long? = null,
    @SerializedName("total_paid") val totalPaid: Long? = null,
    @SerializedName("balance") val balance: Long? = null,
    @SerializedName("pending_payments") val pendingPayments: Int? = null,
    @SerializedName("pending_amount") val pendingAmount: Long? = null,
    @SerializedName("invoices") val invoices: List<MyInvoiceDto> = emptyList()
)

/** آیتم فاکتور در me/finance */
data class MyInvoiceDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("invoice_number") val invoiceNumber: String? = null,
    @SerializedName("invoice_type") val invoiceType: String? = null,    // monthly | ...
    @SerializedName("period_start_date") val periodStartDate: String? = null,
    @SerializedName("period_end_date") val periodEndDate: String? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("status") val status: String? = null,              // open | partial | paid | ...
    @SerializedName("total_amount") val totalAmount: Long? = null,
    @SerializedName("paid_amount") val paidAmount: Long? = null,
    @SerializedName("remaining_amount") val remainingAmount: Long? = null
)
