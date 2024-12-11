package org.uooc.compose.core.database.dao

import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.uooc.compose.core.database.DownloadChunk
import org.uooc.compose.core.database.DownloadTask
import org.uooc.compose.core.database.FileNameByTaskId
import org.uooc.compose.core.database.GetActiveDownloadTasksWithChunks
import org.uooc.compose.core.database.GetAllTasksAndChunks
import org.uooc.compose.core.database.GetTaskAndChunksById
import org.uooc.compose.core.database.GetTasksAndChunksByResourceId
import org.uooc.compose.core.database.SearchHistory
import org.uooc.compose.core.database.core.database.AppDatabase
import org.uooc.compose.core.uoocDispatchers

class UoocDao(
    private val appDatabase: AppDatabase
) {
    private val query get() = appDatabase.user_sessionQueries


    suspend fun clearUserSession() = withContext(uoocDispatchers.io) {
        query.deleteUserSession()
    }

    private val searchingQuery get() = appDatabase.search_historyQueries

    suspend fun addSearchKeyword(keyword: String) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        searchingQuery.transaction {
            searchingQuery.insertSearchKeyword(keyword, currentTime)
            searchingQuery.deleteOldRecordsIfNeeded() // 删除超出限制的记录
        }
    }

    suspend fun cleanSearchKeyword() {
        searchingQuery.cleanSearchKeyword()
    }

    suspend fun getSearchHistory(): List<SearchHistory> {
        return searchingQuery.getSearchHistory().executeAsList()
    }

    private val downloadQuery get() = appDatabase.download_taskQueries

    // 新增下载任务
    suspend fun insertDownloadTask(
        id: String, url: String, resourceId: String, resourceName: String,
        filePath: String, category: String,taskType: Int,
        examType: Int,
        level: Int,
        totalChunks: Long,
        totalSize: Long, showName: String, fileName: String? = null,
        status: DownloadTaskStatus = DownloadTaskStatus.PENDING
    ) {
        val nameAndSize = FileNameByTaskId(fileName ?: "", totalSize)
        downloadQuery.insertDownloadTaskIfNotExists(
            id = id,
            url = url,
            resourceId = resourceId,
            resourceName = resourceName,
            filePath = filePath,
            taskType = taskType.toLong(),
            category = category,
            examType = examType.toLong(),
            level = level.toLong(),
            status = status.getName(),
            totalChunks = totalChunks,
            totalSize = nameAndSize.totalSize, showName = showName,
            fileName = nameAndSize.fileName,
        )
    }

    suspend fun deleteAllTasks() {
        appDatabase.transaction {
            downloadQuery.deleteAllTasks()
        }
    }

    // 通过任务id更新任务状态
    suspend fun updateTaskStatus(
        id: String, resourceId: String, status: DownloadTaskStatus,
        downloadLength: Long = 0,
        fileName: String? = null,
        totalSize: Long? = null,
    ) {
        try {
            val nameAndSize = if (fileName == null) {
                downloadQuery.fileNameByTaskId(
                    id = id,
                    resourceId = resourceId
                ).executeAsOneOrNull()?.let {
                    FileNameByTaskId(it.fileName, totalSize ?: it.totalSize)
                } ?: FileNameByTaskId("", totalSize ?: 0)
            } else {
                downloadQuery.fileNameByTaskId(
                    id = id,
                    resourceId = resourceId
                ).executeAsOneOrNull()?.let {
                    FileNameByTaskId(fileName, totalSize ?: it.totalSize)
                } ?: FileNameByTaskId(fileName, totalSize ?: 0)
            }

            downloadQuery.updateTaskStatus(
                status = status.getName(),
                fileName = nameAndSize.fileName,
                totalSize = nameAndSize.totalSize,
                totalSize_ = downloadLength,
                id = id,
                resourceId = resourceId,
            )
        } catch (e: Exception) {
        }

    }

    // 通过资源包id查询任务列表
    suspend fun getTasksByResourceId(resourceId: String): List<DownloadTask> {
        return downloadQuery.getTasksByResourceId(resourceId).executeAsList()
    }

    suspend fun getTasksAndChunksByResourceId(resourceId: String): List<GetTasksAndChunksByResourceId> {
        return downloadQuery.getTasksAndChunksByResourceId(resourceId).executeAsList()
    }

    suspend fun getAllTasksAndChunks(): List<GetAllTasksAndChunks> {
        return downloadQuery.getAllTasksAndChunks().executeAsList()
    }

    // 通过任务id插入下载分片
    suspend fun insertDownloadChunk(
        chunkId: String,
        taskId: String,
        resourceId: String,
        chunkIndex: Int,
        startByte: Long,
        endByte: Long,
    ) {
        downloadQuery.insertDownloadChunkIfNotExists(
            chunkId,
            taskId, resourceId,
            chunkIndex.toLong(),
            startByte,
            endByte,
        )
    }

    suspend fun updateChunkProgress(
        taskId: String,
        resourceId: String,
        chunkId: String,
        progress: Long
    ) {
        downloadQuery.updateChunkProgress(currentByte = progress, chunkId, taskId, resourceId, currentByte_ = progress)
    }

    // 通过任务id查询分片列表
    suspend fun getChunksByTaskId(taskId: String, resourceId: String): List<DownloadChunk> {
        return downloadQuery.getChunksByTaskId(taskId, resourceId).executeAsList()
    }

    suspend fun getTaskById(indexId: String, resourceId: String): DownloadTask? {
        return downloadQuery.getTaskById(indexId, resourceId).executeAsOneOrNull()
    }

    suspend fun getTaskAndChunksById(indexId: String, resourceId: String): GetTaskAndChunksById? {
        return downloadQuery.getTaskAndChunksById(indexId, resourceId).executeAsOneOrNull()
    }

    suspend fun updateAllDownloadingTasksToPaused() {
        downloadQuery.updateAllDownloadingTasksToPaused()
    }

    suspend fun removeTasks(tasks: List<DownloadTask>) {
        if (tasks.isEmpty()) return
        // 执行批量删除操作
        appDatabase.transaction {
            tasks.forEach {
                downloadQuery.deleteTaskAndChunksById(it.id, it.resourceId)
            }
        }
    }

    // 全部暂停或开始
    suspend fun updateAllTasksStatus(status: DownloadTaskStatus) {
        downloadQuery.updateAllTasksStatus(status.getName())
    }

    // 通过资源包id和分类查询任务列表
    suspend fun getTasksByResourceIdAndCategory(
        resourceId: String,
        category: String
    ): List<DownloadTask> {
        return downloadQuery.getTasksByResourceIdAndCategory(resourceId, category).executeAsList()
    }

    suspend fun deleteChunksById(taskId: String, resourceId: String) {
        downloadQuery.deleteChunksById(taskId, resourceId)
    }

    // 获取最新的正在下载中的任务
    suspend fun getActiveDownloadTasks(): List<DownloadTask> {
        return downloadQuery.getActiveDownloadTasks().executeAsList()
    }

    suspend fun getActiveDownloadTasksWithChunks(): List<GetActiveDownloadTasksWithChunks> {
        return downloadQuery.getActiveDownloadTasksWithChunks().executeAsList()
    }

    // 通过任务id删除单个任务及其所有分片
    suspend fun deleteTaskAndChunksById(taskId: String, resourceId: String) {
        downloadQuery.deleteTaskAndChunksById(taskId, resourceId)
    }

    // 通过资源包id删除其中所有任务及其所有分片
    suspend fun deleteTasksAndChunksByResourceId(resourceId: String) {
        downloadQuery.deleteTasksAndChunksByResourceId(resourceId)
    }
}

enum class DownloadTaskStatus {
    PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED;

    companion object {
        // 通过名称获取枚举
        fun fromName(name: String): DownloadTaskStatus {
            return valueOf(name.uppercase())
        }
        /*fun valueOf(name: String): DownloadTaskStatus {
            return valueOf(name.uppercase())
        }*/
    }

    fun getName(): String {
        return this.name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}