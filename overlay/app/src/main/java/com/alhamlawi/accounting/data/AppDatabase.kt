package com.alhamlawi.accounting.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alhamlawi.accounting.security.DatabaseKeyManager
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities=[UserEntity::class,ExpenseEntity::class,DoctorEntity::class,DoctorPaymentEntity::class,SuspenseEntity::class,DebtEntity::class,DebtPaymentEntity::class,DebtAdjustmentEntity::class,AttachmentEntity::class,AuditLogEntity::class,SettingEntity::class],
    version=2,
    exportSchema=true
)
abstract class AppDatabase:RoomDatabase(){
    abstract fun userDao():UserDao
    abstract fun expenseDao():ExpenseDao
    abstract fun doctorDao():DoctorDao
    abstract fun doctorPaymentDao():DoctorPaymentDao
    abstract fun suspenseDao():SuspenseDao
    abstract fun debtDao():DebtDao
    abstract fun attachmentDao():AttachmentDao
    abstract fun auditDao():AuditDao
    abstract fun settingsDao():SettingsDao
    companion object {
        fun create(context:Context,keyManager:DatabaseKeyManager):AppDatabase {
            System.loadLibrary("sqlcipher")
            val factory=SupportOpenHelperFactory(keyManager.databasePassphrase(), null, false)
            return Room.databaseBuilder(context.applicationContext,AppDatabase::class.java,"alhamlawi_accounting_v2.db")
                .openHelperFactory(factory).fallbackToDestructiveMigrationFrom(1).build()
        }
    }
}
