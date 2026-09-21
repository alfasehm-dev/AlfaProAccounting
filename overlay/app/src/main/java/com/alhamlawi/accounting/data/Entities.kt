package com.alhamlawi.accounting.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object Roles {
    const val MANAGER = "MANAGER"
    const val ACCOUNTANT = "ACCOUNTANT"
    const val READER = "READER"
    val all = listOf(MANAGER, ACCOUNTANT, READER)
}

object ExpenseCategories {
    val all = listOf("محاليل مختبر", "مشتريات طبية", "مشتريات أخرى", "دعم رواتب مكتب الصحة", "تسديد التزامات سابقة", "أخرى")
}
object DoctorPaymentTypes { val all = listOf("نسب أطباء", "مواصلات أطباء") }
object SuspenseStatuses { const val PENDING="معلق"; const val PAID="مدفوع"; val all=listOf(PENDING,PAID) }
object DebtDirections { const val CREDIT="دائن"; const val DEBIT="مدين"; val all=listOf(CREDIT,DEBIT) }

@Entity(tableName="users", indices=[Index(value=["username"], unique=true)])
data class UserEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val username:String,
    val passwordHash:String,
    val passwordSalt:String,
    val role:String=Roles.MANAGER,
    val active:Boolean=true,
    val createdAt:Long=System.currentTimeMillis(),
    val lastLoginAt:Long?=null
)

@Entity(tableName="expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val category:String,
    val description:String,
    val amount:Long,
    val transactionDate:String,
    val notes:String="",
    val createdBy:Long,
    val createdAt:Long=System.currentTimeMillis(),
    val updatedAt:Long=System.currentTimeMillis(),
    val deletedAt:Long?=null,
    val deleteReason:String=""
)

@Entity(tableName="doctors", indices=[Index(value=["name"], unique=true)])
data class DoctorEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val name:String,
    val phone:String="",
    val active:Boolean=true
)

@Entity(tableName="doctor_payments", indices=[Index("doctorId")])
data class DoctorPaymentEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val doctorId:Long?=null,
    val doctorName:String,
    val type:String,
    val amount:Long,
    val percentageBasisPoints:Int?=null,
    val description:String="",
    val transactionDate:String,
    val createdBy:Long,
    val createdAt:Long=System.currentTimeMillis(),
    val deletedAt:Long?=null,
    val deleteReason:String=""
) {
    val percentage: Double? get() = percentageBasisPoints?.div(100.0)
}

@Entity(tableName="suspenses")
data class SuspenseEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val description:String,
    val amount:Long,
    val status:String=SuspenseStatuses.PENDING,
    val transactionDate:String,
    val paidDate:String?=null,
    val notes:String="",
    val createdBy:Long,
    val paidBy:Long?=null,
    val createdAt:Long=System.currentTimeMillis(),
    val updatedAt:Long=System.currentTimeMillis(),
    val deletedAt:Long?=null,
    val deleteReason:String=""
)

@Entity(tableName="debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val partyName:String,
    val doctorId:Long?=null,
    val category:String,
    val direction:String,
    val originalAmount:Long,
    val transactionDate:String,
    val dueDate:String,
    val description:String="",
    val notes:String="",
    val createdBy:Long,
    val createdAt:Long=System.currentTimeMillis(),
    val deletedAt:Long?=null,
    val deleteReason:String=""
)

@Entity(tableName="debt_payments", foreignKeys=[ForeignKey(entity=DebtEntity::class,parentColumns=["id"],childColumns=["debtId"],onDelete=ForeignKey.RESTRICT)], indices=[Index("debtId")])
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val debtId:Long,
    val amount:Long,
    val paymentDate:String,
    val description:String="",
    val createdBy:Long,
    val createdAt:Long=System.currentTimeMillis()
)

@Entity(tableName="debt_adjustments", foreignKeys=[ForeignKey(entity=DebtEntity::class,parentColumns=["id"],childColumns=["debtId"],onDelete=ForeignKey.RESTRICT)], indices=[Index("debtId")])
data class DebtAdjustmentEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val debtId:Long,
    val adjustmentAmount:Long,
    val reason:String,
    val createdBy:Long,
    val createdAt:Long=System.currentTimeMillis()
)

@Entity(tableName="attachments", indices=[Index(value=["entityType","entityId"])])
data class AttachmentEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val entityType:String,
    val entityId:Long,
    val fileName:String,
    val mimeType:String,
    val encryptedPath:String,
    val createdAt:Long=System.currentTimeMillis()
)

@Entity(tableName="audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate=true) val id:Long=0,
    val userId:Long,
    val action:String,
    val entityType:String,
    val entityId:Long,
    val oldValues:String="",
    val newValues:String="",
    val timestamp:Long=System.currentTimeMillis()
)

@Entity(tableName="settings")
data class SettingEntity(@PrimaryKey val key:String, val value:String)

data class DebtWithPaid(
    val id:Long,
    val partyName:String,
    val doctorId:Long?,
    val category:String,
    val direction:String,
    val originalAmount:Long,
    val transactionDate:String,
    val dueDate:String,
    val description:String,
    val notes:String,
    val createdBy:Long,
    val createdAt:Long,
    val deletedAt:Long?,
    val deleteReason:String,
    val paidAmount:Long,
    val adjustmentAmount:Long
) {
    val remainingAmount:Long get()=(originalAmount - paidAmount + adjustmentAmount).coerceAtLeast(0)
    val status:String get()=when {
        remainingAmount==0L -> "مسدد بالكامل"
        dueDate < java.time.LocalDate.now().toString() -> "متأخر"
        paidAmount>0L -> "مسدد جزئياً"
        else -> "غير مسدد"
    }
}
