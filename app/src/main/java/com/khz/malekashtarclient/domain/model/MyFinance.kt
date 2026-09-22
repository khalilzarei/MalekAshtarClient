package com.khz.malekashtarclient.domain.model

data class MyFinance(
    val playerId: Int,
    val playerName: String,
    val totalInvoiced: Long,
    val totalPaid: Long,
    val balance: Long,
    val debt: Long,
    val isDebtor: Boolean,
    val pendingPayments: Int,
    val pendingAmount: Long,
    val invoices: List<MyInvoice>,
    val classDebts: List<ClassDebt>,
    val classFees: List<ClassFee>
)

data class MyInvoice(
    val id: Int,
    val invoiceNumber: String?,
    val invoiceType: String?,
    val periodStartDate: String?,
    val periodEndDate: String?,
    val dueDate: String?,
    val status: String?,
    val subtotal: Long,
    val discountTotal: Long,
    val totalAmount: Long,
    val paidAmount: Long,
    val remainingAmount: Long,
    val notes: String?,
    val items: List<InvoiceItem>
)

data class InvoiceItem(
    val id: Int,
    val title: String,
    val itemType: String?,
    val amount: Long,
    val quantity: Int,
    val total: Long,
    val classId: Int?,
    val classTitle: String?,
    val ageGroupTitle: String?,
    val description: String?
)

data class ClassDebt(
    val classId: Int?,
    val classTitle: String,
    val ageGroupTitle: String?,
    val total: Long,
    val paid: Long,
    val remaining: Long,
    val itemsCount: Int
)

data class ClassFee(
    val classId: Int?,
    val classTitle: String,
    val ageGroupTitle: String?,
    val monthlyFee: Long?,
    val sessionFee: Long?,
    val registrationFee: Long?,
    val enrolled: Boolean,
    val debt: Long,
    val paid: Long,
    val total: Long
)
