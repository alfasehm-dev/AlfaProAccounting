package com.alhamlawi.accounting

import android.app.Application
import com.alhamlawi.accounting.backup.BackupManager
import com.alhamlawi.accounting.data.AppDatabase
import com.alhamlawi.accounting.data.AppRepository
import com.alhamlawi.accounting.export.ReportExporter
import com.alhamlawi.accounting.security.AttachmentStore
import com.alhamlawi.accounting.security.DatabaseKeyManager

class AlHamlawiApp:Application(){
    lateinit var keyManager:DatabaseKeyManager
        private set
    lateinit var attachmentStore:AttachmentStore
        private set
    lateinit var database:AppDatabase
        private set
    lateinit var repository:AppRepository
        private set
    lateinit var backupManager:BackupManager
        private set
    lateinit var reportExporter:ReportExporter
        private set

    override fun onCreate(){
        super.onCreate()
        keyManager=DatabaseKeyManager(this)
        attachmentStore=AttachmentStore(this,keyManager)
        database=AppDatabase.create(this,keyManager)
        repository=AppRepository(database, attachmentStore)
        backupManager=BackupManager(this,database,attachmentStore)
        reportExporter=ReportExporter(this)
    }
}
