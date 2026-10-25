package com.example.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import com.example.data.model.AttachedFile
import com.example.data.model.ProjectWorkspace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class ProjectManager(private val context: Context) {

    /**
     * Lists child documents or files from a persisted Scoped Storage Document Tree URI.
     */
    suspend fun listFilesFromTreeUri(treeUri: Uri): List<AttachedFile> = withContext(Dispatchers.IO) {
        val fileList = mutableListOf<AttachedFile>()
        try {
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )

            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_SIZE
            )

            val cursor: Cursor? = context.contentResolver.query(
                childrenUri,
                projection,
                null,
                null,
                null
            )

            cursor?.use { c ->
                val idIndex = c.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIndex = c.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIndex = c.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val sizeIndex = c.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)

                while (c.moveToNext()) {
                    val docId = c.getString(idIndex)
                    val name = c.getString(nameIndex)
                    val mime = c.getString(mimeIndex)
                    val size = c.getLong(sizeIndex)

                    val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    fileList.add(
                        AttachedFile(
                            id = docId,
                            name = name,
                            content = "",
                            sizeBytes = size,
                            mimeType = mime,
                            uriString = docUri.toString()
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // In case of permission revocation or invalid tree
        }
        fileList
    }

    /**
     * Reads the text content of a selected file URI.
     */
    suspend fun readFileContent(uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext ""
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            "Error al leer archivo: ${e.localizedMessage}"
        }
    }

    /**
     * Default starter projects in /MisProyectos
     */
    fun getDefaultProjects(): List<ProjectWorkspace> {
        return listOf(
            ProjectWorkspace(
                name = "AppAndroid-OpenCode",
                path = "/MisProyectos/AppAndroid-OpenCode",
                files = listOf("MainActivity.kt", "ChatViewModel.kt", "OpenRouterClient.kt"),
                isLinked = false
            ),
            ProjectWorkspace(
                name = "ApiBackend-FastAPI",
                path = "/MisProyectos/ApiBackend-FastAPI",
                files = listOf("main.py", "routers/chat.py", "requirements.txt"),
                isLinked = false
            ),
            ProjectWorkspace(
                name = "Microservicio-Go",
                path = "/MisProyectos/Microservicio-Go",
                files = listOf("main.go", "handler.go", "go.mod"),
                isLinked = false
            )
        )
    }
}
