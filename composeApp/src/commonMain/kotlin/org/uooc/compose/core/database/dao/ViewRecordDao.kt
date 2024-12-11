package org.uooc.compose.core.database.dao

import org.uooc.compose.core.database.ViewingRecordModel
import org.uooc.compose.core.database.core.database.AppDatabase

class ViewRecordDao(
    private val appDatabase: AppDatabase
) {
    private val query get() = appDatabase.view_recordQueries
    private val downloadQuery get() = appDatabase.download_taskQueries

    suspend fun addViewRecord(
        model: ViewingRecordModel,
        resourceId: String, taskId: String, studentId: String
    ) {
        appDatabase.transaction {
            query.insertRecord(
                action = model.action,
                timestamp = model.timestamp,
                watchTime = model.watchTime,
                fid = model.fid,
                operator_ = model.operator_,
                recordId = model.recordId,
                progress = model.progress
            )
            downloadQuery.updateTaskStatusWithRecord(
                recordId = model.recordId,
                id = taskId,
                resourceId = resourceId,
                recordStudentReleativeId = studentId,
                lastProgress = model.progress.toFloat().toDouble(),
            )
        }
    }

    suspend fun updateLastViewRecord(
        model: ViewingRecordModel,
        resourceId: String, taskId: String, studentId: String
    ) {
        appDatabase.transaction {
            downloadQuery.updateTaskStatusWithRecord(
                recordId = model.recordId,
                id = taskId,
                resourceId = resourceId,
                recordStudentReleativeId = studentId,
                lastProgress = model.progress.toFloat().toDouble(),
            )
            query.cleanRecord(model.operator_)
        }
    }

    suspend fun getAllRecord(operator: String) = query.allRecord(operator).executeAsList()

    suspend fun deleteAllRecord(operator: String) = query.cleanRecord(operator)

    suspend fun cleanAllRecord() = query.cleanAllRecord()

}