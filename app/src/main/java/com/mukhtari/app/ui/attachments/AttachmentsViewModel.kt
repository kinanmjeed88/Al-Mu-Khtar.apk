package com.mukhtari.app.ui.attachments

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukhtari.app.data.local.dao.AttachmentDao
import com.mukhtari.app.data.local.entity.AttachmentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AttachmentsViewModel(
    private val attachmentDao: AttachmentDao,
    private val context: Context
) : ViewModel() {

    private val _attachments = MutableStateFlow<List<AttachmentEntity>>(emptyList())
    val attachments: StateFlow<List<AttachmentEntity>> = _attachments.asStateFlow()

    fun loadAttachments(ownerType: String, ownerId: Long) {
        viewModelScope.launch {
            attachmentDao.getAttachmentsForOwner(ownerType, ownerId).collect {
                _attachments.value = it
            }
        }
    }

    fun saveAttachment(uri: Uri, ownerType: String, ownerId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var fileName = "unknown"
                var fileSize: Long = 0
                val mimeType = context.contentResolver.getType(uri)

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex)
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }

                val attachmentsDir = File(context.filesDir, "attachments")
                if (!attachmentsDir.exists()) attachmentsDir.mkdirs()

                val destFile = File(attachmentsDir, "${System.currentTimeMillis()}_$fileName")

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                val attachment = AttachmentEntity(
                    ownerType = ownerType,
                    ownerId = ownerId,
                    fileType = destFile.extension.ifEmpty { "unknown" },
                    mimeType = mimeType,
                    filePath = destFile.absolutePath,
                    fileName = fileName,
                    fileSize = fileSize,
                    notes = null,
                    isDeleted = 0,
                    deletedAt = null,
                    deletedReason = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                attachmentDao.insert(attachment)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAttachment(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val attachment = attachmentDao.getById(id)
                if (attachment != null) {
                    val file = File(attachment.filePath)
                    if (file.exists()) {
                        // Check if file is referenced by others before deleting the actual file
                        val refCount = attachmentDao.getActiveReferencesCount(attachment.filePath, id)
                        if (refCount == 0) {
                            file.delete()
                        }
                    }
                    attachmentDao.hardDelete(id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
