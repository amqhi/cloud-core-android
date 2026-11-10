/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.amqhi.cloud.core.EventCode.NETWORK_CONNECTION_FAILURE
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import okio.BufferedSink
import okio.buffer
import okio.sink
import okio.source
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import com.amqhi.shared.core.NetworkCallback

interface FileProgressListener {
    fun onDownloadStarted(itemId: UUID, parentId: UUID) {}
    fun onUploadStarted(itemId: UUID, parentId: UUID) {}
    fun onDownloadProgress(itemId: UUID, parentId: UUID, bytesReceived: Long, totalBytes: Long) {}
    fun onUploadProgress(itemId: UUID, parentId: UUID, name: String, bytesWritten: Long, totalBytes: Long) {}

    fun onDownloadFinished(itemId: UUID, parentId: UUID) {}
    fun onUploadFinished(itemId: UUID, parentId: UUID) {}
}

object TransferType {
    const val FILE: Short = 0
    const val THUMBNAIL: Short = 1
    const val MULTIPART_FILE: Short = 2
}

class NetworkProvider {

    private val fileProgressListeners = CopyOnWriteArrayList<FileProgressListener>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val completedMultipartBytes = mutableMapOf<UUID, Long>()

    fun registerFileProgressListener(listener: FileProgressListener) {
        if (!fileProgressListeners.contains(listener)) {
            fileProgressListeners.add(listener)
        }
    }

    fun unregisterFileProgressListener(listener: FileProgressListener) {
        fileProgressListeners.remove(listener)
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(0, TimeUnit.SECONDS)
        .build()

    fun get(url: String, headers: Map<String, String>, callback: NetworkCallback) {
        enqueueJson(url, headers, callback) { it.get() }
    }

    fun post(url: String, headers: Map<String, String>, callback: NetworkCallback) {
        enqueueJson(url, headers, callback) { it.post("{}".toRequestBody(jsonMediaType)) }
    }

    fun postJson(url: String, headers: Map<String, String>, body: String, callback: NetworkCallback) {
        enqueueJson(url, headers, callback) { it.post(body.toRequestBody(jsonMediaType)) }
    }

    fun patchJson(url: String, headers: Map<String, String>, body: String, callback: NetworkCallback) {
        enqueueJson(url, headers, callback) { it.patch(body.toRequestBody(jsonMediaType)) }
    }

    fun destroy(url: String, headers: Map<String, String>, callback: NetworkCallback) {
        enqueueJson(url, headers, callback) { it.delete() }
    }

    fun putFile(
        url: String,
        headers: Map<String, String>,
        filePath: String,
        taskInfoRaw: String,
        callback: NetworkCallback
    ) {
        if (!isHttpUrl(url)) return

        val taskInfo = JSONObject(taskInfoRaw)
        val type = taskInfo.getInt("type").toShort()

        val file = File(filePath)

        if(type == TransferType.FILE || type == TransferType.MULTIPART_FILE) {
            val id = UUID.fromString(taskInfo.getString("id"))
            val parentId = UUID.fromString(taskInfo.getString("parent_id"))
            mainHandler.post {
                fileProgressListeners.forEach { listener ->
                    listener.onUploadStarted(id, parentId)
                }
            }
        }

        val onProgress : (Long, Long) -> Unit = when(type) {
            TransferType.MULTIPART_FILE -> { bytesWritten, _ ->
                    mainHandler.post {
                        val id = UUID.fromString(taskInfo.getString("id"))
                        val parentId = UUID.fromString(taskInfo.getString("parent_id"))
                        val name = taskInfo.optString("name")
                        fileProgressListeners.forEach {
                            it.onUploadProgress(
                                id,
                                parentId,
                                name,
                                bytesWritten + completedMultipartBytes[id]!!,
                                taskInfo.getString("file_size").toLong()
                            )
                        }
                    }
        }
            else ->  { bytesWritten, totalBytes ->
                mainHandler.post {
                    val id = UUID.fromString(taskInfo.getString("id"))
                    val parentId = UUID.fromString(taskInfo.getString("parent_id"))
                    val name = taskInfo.optString("name")
                    fileProgressListeners.forEach { it.onUploadProgress(id, parentId, name, bytesWritten, totalBytes) }
                }
            }
        }

        val requestBody = ProgressRequestBody(file, headers, onProgress)
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .applyHeaders(headers)
            .build()

        if(type == TransferType.MULTIPART_FILE) {
            val id = UUID.fromString(taskInfo.getString("id"))
            if(!completedMultipartBytes.containsKey(id)) {
                completedMultipartBytes[id] = 0
            }
            client.newCall(request).enqueue(callback.toMultipartOkHttpCallback(
                itemId = id,
                fileSize = taskInfo.getString("file_size").toLong(),
                chunkSize = file.length()
            ))
        }
        else {
            client.newCall(request).enqueue(callback.toOkHttpCallback())
        }
    }

fun downloadFile(
    url: String,
    headers: Map<String, String>,
    filePath: String,
    taskInfoRaw: String,
    callback: NetworkCallback
) {
    if (!isHttpUrl(url)) return

    val request = Request.Builder()
        .url(url)
        .applyHeaders(headers)
        .get()
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            response.use { res ->
                val code = res.code
                val body = res.body

                if (!res.isSuccessful || body == null) {
                    postResponse(callback, code, filePath, mapOf())
                    return
                }

                val file = File(filePath)
                try {
                    file.parentFile?.mkdirs()
                    val totalBytes = body.contentLength()
                    var downloadedBytes = 0L
                    var lastProgressTime = 0L
                    val taskInfo = JSONObject(taskInfoRaw)
                    val type = taskInfo.optInt("type").toShort()

                    if(type == TransferType.FILE) {
                        val id = UUID.fromString(taskInfo.getString("id"))
                        val parentId = UUID.fromString(taskInfo.getString("parent_id"))
                        mainHandler.post {
                            fileProgressListeners.forEach { listener ->
                                listener.onDownloadStarted(id, parentId)
                            }
                        }
                    }

                    body.source().use { source ->
                        file.sink().buffer().use { sink ->
                            val bufferSize = 8_192L
                            var bytesRead: Long
                            val buffer = Buffer()
                            val id = UUID.fromString(taskInfo.getString("id"))
                            val parentId = UUID.fromString(taskInfo.getString("parent_id"))

                            if(type == TransferType.FILE) {
                                while (source.read(buffer, bufferSize).also { bytesRead = it } != -1L) {

                                    val currentTime = System.currentTimeMillis()

                                    sink.write(buffer, bytesRead)

                                    downloadedBytes += bytesRead
                                    if (totalBytes > 0 && (currentTime - lastProgressTime > 100 || downloadedBytes == totalBytes)) {
                                        lastProgressTime = currentTime
                                        mainHandler.post {
                                            fileProgressListeners.forEach { listener ->
                                                listener.onDownloadProgress(id, parentId, downloadedBytes, totalBytes)
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                while (source.read(buffer, bufferSize).also { bytesRead = it } != -1L) {
                                    sink.write(buffer, bytesRead)
                                }
                            }
                            sink.flush()
                        }
                    }
                    postResponse(callback, code, filePath, mapOf())
                } catch (e: IOException) {
                    Log.e(TAG, "Download failed", e)
                    if (file.exists()) { file.delete() }
                    postFailure(callback, -1, e.message ?: "Download write error")
                }
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            Log.e(TAG, "Download request failed", e)
            postFailure(callback, -1, e.message ?: "Network error")
        }
    })
}

    private fun enqueueJson(
        url: String,
        headers: Map<String, String>,
        callback: NetworkCallback,
        applyMethod: (Request.Builder) -> Request.Builder
    ) {
        if (!isHttpUrl(url)) return
        val request = applyMethod(
            Request.Builder().url(url).applyHeaders(headers)
        ).build()
        client.newCall(request).enqueue(callback.toOkHttpCallback())
    }

    private fun isHttpUrl(url: String): Boolean = url.isNotEmpty() && !url.startsWith("/")

    private fun Request.Builder.applyHeaders(headers: Map<String, String>): Request.Builder {
        headers.forEach { (key, value) -> addHeader(key, value) }
        return this
    }

    private fun NetworkCallback.toOkHttpCallback() = object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val code = response.code
            val body = response.body?.string().orEmpty()
            response.close()
            mainHandler.post { onResponse(code, body, response.headers.toMap()) }
        }

        override fun onFailure(call: Call, e: IOException) {
            Log.e(TAG, "Request failed", e)
            mainHandler.post { onFailure(NETWORK_CONNECTION_FAILURE, e.message ?: "Network error") }
        }
    }

    private fun NetworkCallback.toMultipartOkHttpCallback(itemId: UUID, fileSize: Long, chunkSize: Long) = object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val code = response.code
            val body = response.body?.string().orEmpty()
            response.close()
            completedMultipartBytes[itemId] = completedMultipartBytes[itemId]!! + chunkSize
            if(completedMultipartBytes[itemId]!! >= fileSize) {
                completedMultipartBytes.remove(itemId)
            }
            mainHandler.post { onResponse(code, body, response.headers.toMap()) }
        }

        override fun onFailure(call: Call, e: IOException) {
            Log.e(TAG, "Request failed", e)
            mainHandler.post { onFailure(NETWORK_CONNECTION_FAILURE, e.message ?: "Network error") }
        }
    }

    private fun postResponse(callback: NetworkCallback, code: Int, body: String, headers: Map<String, String>) {
        mainHandler.post { callback.onResponse(code, body, headers) }
    }

    private fun postFailure(callback: NetworkCallback, errorCode: Short, data: String) {
        mainHandler.post { callback.onFailure(errorCode, data) }
    }

    private companion object {
        const val TAG = "NetworkProvider"
    }
}

private class ProgressRequestBody(
    private val file: File,
    private val headers: Map<String, String>,
    private val onProgress: (Long, Long) -> Unit
) : RequestBody() {
    override fun contentType() = headers["Content-Type"]?.toMediaType()
    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = file.length()
        var uploadedBytes = 0L
        file.source().use { source ->
            var bytesRead: Long
            var lastProgressTime = 0L
            while (source.read(sink.buffer, 8_192L).also { bytesRead = it } != -1L) {
                sink.flush()
                uploadedBytes += bytesRead
                val currentTime = System.currentTimeMillis()
                if(totalBytes > 0 && (currentTime - lastProgressTime > 100 || uploadedBytes == totalBytes)) {
                    lastProgressTime = currentTime
                    onProgress(uploadedBytes, totalBytes)
                }
            }
        }
    }
}
