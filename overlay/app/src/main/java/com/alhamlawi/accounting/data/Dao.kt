package com.alhamlawi.accounting.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface UserDao {
    @Query("SELECT COUNT(*) FROM users") suspend fun count():Int
    @Query("SELECT * FROM users WHERE username=:username AND active=1 LIMIT 1") suspend fun byUsername(username:String):UserEntity?
    @Query("SELECT * FROM users ORDER BY username") fun observeAll():Flow<List<UserEntity>>
    @Query("SELECT * FROM users ORDER BY username") suspend fun snapshot():List<UserEntity>
    @Insert(onConflict=OnConflictStrategy.ABORT) suspend fun insert(user:UserEntity):Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun restore(user:UserEntity):Long
    @Update suspend fun update(user:UserEntity)
    @Query("UPDATE users SET lastLoginAt=:time WHERE id=:id") suspend fun markLogin(id:Long,time:Long)
    @Query("DELETE FROM users") suspend fun clear()
}

@Dao interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE deletedAt IS NULL ORDER BY transactionDate DESC,id DESC") fun observeAll():Flow<List<ExpenseEntity>>
    @Query("SELECT * FROM expenses ORDER BY id") suspend fun snapshot():List<ExpenseEntity>
    @Insert suspend fun insert(item:ExpenseEntity):Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun restore(item:ExpenseEntity):Long
    @Update suspend fun update(item:ExpenseEntity)
    @Query("SELECT * FROM expenses WHERE id=:id LIMIT 1") suspend fun byId(id:Long):ExpenseEntity?
    @Query("UPDATE expenses SET deletedAt=:deletedAt,deleteReason=:reason,updatedAt=:deletedAt WHERE id=:id") suspend fun softDelete(id:Long,deletedAt:Long,reason:String)
    @Query("DELETE FROM expenses") suspend fun clear()
}

@Dao interface DoctorDao {
    @Query("SELECT * FROM doctors WHERE active=1 ORDER BY name") fun observeActive():Flow<List<DoctorEntity>>
    @Query("SELECT * FROM doctors ORDER BY id") suspend fun snapshot():List<DoctorEntity>
    @Insert(onConflict=OnConflictStrategy.IGNORE) suspend fun insert(item:DoctorEntity):Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun restore(item:DoctorEntity):Long
    @Query("DELETE FROM doctors") suspend fun clear()
}

@Dao interface DoctorPaymentDao {
    @Query("SELECT * FROM doctor_payments WHERE deletedAt IS NULL ORDER BY transactionDate DESC,id DESC") fun observeAll():Flow<List<DoctorPaymentEntity>>
    @Query("SELECT * FROM doctor_payments ORDER BY id") suspend fun snapshot():List<DoctorPaymentEntity>
    @Insert suspend fun insert(item:DoctorPaymentEntity):Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun restore(item:DoctorPaymentEntity):Long
    @Query("SELECT * FROM doctor_payments WHERE id=:id LIMIT 1") suspend fun byId(id:Long):DoctorPaymentEntity?
    @Query("UPDATE doctor_payments SET deletedAt=:deletedAt,deleteReason=:reason WHERE id=:id") suspend fun softDelete(id:Long,deletedAt:Long,reason:String)
    @Query("DELETE FROM doctor_payments") suspend fun clear()
}

@Dao interface SuspenseDao {
    @Query("SELECT * FROM suspenses WHERE deletedAt IS NULL ORDER BY transactionDate DESC,id DESC") fun observeAll():Flow<List<SuspenseEntity>>
    @Query("SELECT * FROM suspenses ORDER BY id") suspend fun snapshot():List<SuspenseEntity>
    @Insert suspend fun insert(item:SuspenseEntity):Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun restore(item:SuspenseEntity):Long
    @Query("SELECT * FROM suspenses WHERE id=:id LIMIT 1") suspend fun byId(id:Long):SuspenseEntity?
    @Query("UPDATE suspenses SET status=:status,paidDate=:paidDate,paidBy=:paidBy,updatedAt=:updatedAt WHERE id=:id") suspend fun updateStatus(id:Long,status:String,paidDate:String?,paidBy:Long?,updatedAt:Long)
    @Query("UPDATE suspenses SET deletedAt=:deletedAt,deleteReason=:reason,updatedAt=:deletedAt WHERE id=:id") suspend fun softDelete(id:Long,deletedAt:Long,reason:String)
    @Query("DELETE FROM suspenses") suspend fun clear()
}

@Dao interface DebtDao {
    @Query("""SELECT d.*,COALESCE((SELECT SUM(amount) FROM debt_payments p WHERE p.debtId=d.id),0) AS paidAmount,COALESCE((SELECT SUM(adjustmentAmount) FROM debt_adjustments a WHERE a.debtId=d.id),0) AS adjustmentAmount FROM debts d WHERE d.deletedAt IS NULL ORDER BY d.dueDate ASC,d.id DESC""") fun observeWithPaid():Flow<List<DebtWithPaid>>
    @Query("""SELECT d.*,COALESCE((SELECT SUM(amount) FROM debt_payments p WHERE p.debtId=d.id),0) AS paidAmount,COALESCE((SELECT SUM(adjustmentAmount) FROM debt_adjustments a WHERE a.debtId=d.id),0) AS adjustmentAmount FROM debts d WHERE d.deletedAt IS NULL ORDER BY d.dueDate ASC,d.id DESC""") suspend fun snapshotWithPaid():List<DebtWithPaid>
    @Query("SELECT * FROM debts ORDER BY id") suspend fun snapshotDebts():List<DebtEntity>
    @Query("SELECT * FROM debt_payments ORDER BY id") suspend fun snapshotPayments():List<DebtPaymentEntity>
    @Query("SELECT * FROM debt_adjustments ORDER BY id") suspend fun snapshotAdjustments():List<DebtAdjustmentEntity>
    @Query("SELECT * FROM debt_payments ORDER BY paymentDate DESC,id DESC") fun observePayments():Flow<List<DebtPaymentEntity>>
    @Query("SELECT * FROM debt_adjustments ORDER BY createdAt DESC,id DESC") fun observeAdjustments():Flow<List<DebtAdjustmentEntity>>
    @Insert suspend fun insert(item:DebtEntity):Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun restoreDebt(item:DebtEntity):Long
    @Insert suspend fun insertPayment(item:DebtPaymentEntity):Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun restorePayment(item:DebtPaymentEntity):Long
    @Insert suspend fun insertAdjustment(item:DebtAdjustmentEntity):Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun restoreAdjustment(item:DebtAdjustmentEntity):Long
    @Query("SELECT * FROM debts WHERE id=:id LIMIT 1") suspend fun byId(id:Long):DebtEntity?
    @Query("SELECT d.originalAmount - COALESCE((SELECT SUM(amount) FROM debt_payments WHERE debtId=d.id),0) + COALESCE((SELECT SUM(adjustmentAmount) FROM debt_adjustments WHERE debtId=d.id),0) FROM debts d WHERE d.id=:debtId LIMIT 1") suspend fun remaining(debtId:Long):Long?
    @Query("UPDATE debts SET deletedAt=:deletedAt,deleteReason=:reason WHERE id=:id") suspend fun softDelete(id:Long,deletedAt:Long,reason:String)
    @Query("DELETE FROM debt_payments") suspend fun clearPayments()
    @Query("DELETE FROM debt_adjustments") suspend fun clearAdjustments()
    @Query("DELETE FROM debts") suspend fun clearDebts()
}

@Dao interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE entityType=:type AND entityId=:entityId ORDER BY id") fun observeFor(type:String,entityId:Long):Flow<List<AttachmentEntity>>
    @Query("SELECT * FROM attachments ORDER BY id") suspend fun snapshot():List<AttachmentEntity>
    @Insert suspend fun insert(item:AttachmentEntity):Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun restore(item:AttachmentEntity):Long
    @Query("DELETE FROM attachments") suspend fun clear()
}

@Dao interface AuditDao {
    @Insert suspend fun insert(item:AuditLogEntity):Long
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun restore(item:AuditLogEntity):Long
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit") fun observeRecent(limit:Int=500):Flow<List<AuditLogEntity>>
    @Query("SELECT * FROM audit_logs ORDER BY id") suspend fun snapshot():List<AuditLogEntity>
    @Query("DELETE FROM audit_logs") suspend fun clear()
}

@Dao interface SettingsDao {
    @Query("SELECT * FROM settings") fun observeAll():Flow<List<SettingEntity>>
    @Query("SELECT * FROM settings ORDER BY key") suspend fun snapshot():List<SettingEntity>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun put(item:SettingEntity)
    @Query("SELECT value FROM settings WHERE key=:key LIMIT 1") suspend fun value(key:String):String?
    @Query("DELETE FROM settings") suspend fun clear()
}
