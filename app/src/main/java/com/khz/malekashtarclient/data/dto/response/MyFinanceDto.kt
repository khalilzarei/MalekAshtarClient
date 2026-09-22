package com.khz.malekashtarclient.data.dto.response

import com.google.gson.annotations.SerializedName

data class MyFinanceDto(
    @SerializedName("player_id") val playerId: Int? = null,
    @SerializedName("player_name") val playerName: String? = null,
    @SerializedName("total_invoiced") val totalInvoiced: Long? = null,
    @SerializedName("total_paid") val totalPaid: Long? = null,
    @SerializedName("balance") val balance: Long? = null,
    @SerializedName("debt") val debt: Long? = null,
    @SerializedName("is_debtor") val isDebtor: Boolean? = null,
    @SerializedName("pending_payments") val pendingPayments: Int? = null,
    @SerializedName("pending_amount") val pendingAmount: Long? = null,
    @SerializedName("invoices") val invoices: List<MyInvoiceDto> = emptyList(),
    @SerializedName("class_debts") val classDebts: List<ClassDebtDto> = emptyList(),
    @SerializedName("class_fees") val classFees: List<ClassFeeDto> = emptyList()
)

data class MyInvoiceDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("invoice_number") val invoiceNumber: String? = null,
    @SerializedName("invoice_type") val invoiceType: String? = null,
    @SerializedName("period_start_date") val periodStartDate: String? = null,
    @SerializedName("period_end_date") val periodEndDate: String? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("subtotal") val subtotal: Long? = null,
    @SerializedName("discount_total") val discountTotal: Long? = null,
    @SerializedName("total_amount") val totalAmount: Long? = null,
    @SerializedName("paid_amount") val paidAmount: Long? = null,
    @SerializedName("remaining_amount") val remainingAmount: Long? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("items") val items: List<InvoiceItemDto> = emptyList()
)

data class InvoiceItemDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("item_type") val itemType: String? = null,
    @SerializedName("amount") val amount: Long? = null,
    @SerializedName("quantity") val quantity: Int? = null,
    @SerializedName("total") val total: Long? = null,
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("class_title") val classTitle: String? = null,
    @SerializedName("age_group_title") val ageGroupTitle: String? = null,
    @SerializedName("description") val description: String? = null
)

data class ClassDebtDto(
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("class_title") val classTitle: String? = null,
    @SerializedName("age_group_title") val ageGroupTitle: String? = null,
    @SerializedName("total") val total: Long? = null,
    @SerializedName("paid") val paid: Long? = null,
    @SerializedName("remaining") val remaining: Long? = null,
    @SerializedName("items_count") val itemsCount: Int? = null
)

data class ClassFeeDto(
    @SerializedName("class_id") val classId: Int? = null,
    @SerializedName("class_title") val classTitle: String? = null,
    @SerializedName("age_group_title") val ageGroupTitle: String? = null,
    @SerializedName("monthly_fee") val monthlyFee: Long? = null,
    @SerializedName("session_fee") val sessionFee: Long? = null,
    @SerializedName("registration_fee") val registrationFee: Long? = null,
    @SerializedName("enrolled") val enrolled: Boolean? = null,
    @SerializedName("debt") val debt: Long? = null,
    @SerializedName("paid") val paid: Long? = null,
    @SerializedName("total") val total: Long? = null
)
