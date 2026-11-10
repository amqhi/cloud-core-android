/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

import com.amqhi.cloud.core.Core.pointer
import com.amqhi.shared.core.FileMetadata
import com.amqhi.shared.core.FolderMetadata
import com.amqhi.shared.core.Item
import com.amqhi.shared.core.ItemAttributes
import com.amqhi.shared.core.ItemSummary
import java.util.UUID

class ItemManager {
    fun createFolder(attrs: ItemAttributes) = createFolder(pointer, attrs)
    private external fun createFolder(ptr: Long, attrs: ItemAttributes)

    private external fun fetchFileDownloadUrl(ptr: Long, idHigh: Long, idLow: Long)
    fun fetchFileDownloadUrl(id: UUID) = fetchFileDownloadUrl(pointer, id.mostSignificantBits, id.leastSignificantBits)

    private external fun createFile(ptr: Long, itemAttributes: ItemAttributes, tmpFilePath: String)
    fun createFile(itemAttributes: ItemAttributes, tmpFilePath: String) =
        createFile(pointer, itemAttributes, tmpFilePath)

    private external fun getItemSummaryListById(ptr: Long, parentIdHigh: Long, parentIdLow: Long): Array<ItemSummary>
    fun getItemSummaryListById(parentId: UUID): List<ItemSummary> =
        getItemSummaryListById(pointer, parentId.mostSignificantBits, parentId.leastSignificantBits).toList()

    private external fun getItemSummaryById(ptr: Long, idHigh: Long, idLow: Long): ItemSummary
    fun getItemSummaryById(id: UUID): ItemSummary = getItemSummaryById(pointer, id.mostSignificantBits, id.leastSignificantBits)

    private external fun getItemOfflineFilePathById(ptr: Long, idHigh: Long, idLow: Long) : String
    fun getItemOfflineFilePathById(id: UUID): String = getItemOfflineFilePathById(pointer, id.mostSignificantBits, id.leastSignificantBits)
    private external fun getItemById(ptr: Long, idHigh: Long, idLow: Long): Item

    fun getItemById(idHigh: Long, idLow: Long): Item = getItemById(pointer, idHigh, idLow)
    fun getItemById(id: UUID): Item = getItemById(pointer, id.mostSignificantBits, id.leastSignificantBits)

    private external fun getFileMetadataById(ptr: Long, idHigh: Long, idLow: Long): FileMetadata

    fun getFileMetadataById(idHigh: Long, idLow: Long): FileMetadata = getFileMetadataById(pointer, idHigh, idLow)
    fun getFileMetadataById(id: UUID): FileMetadata = getFileMetadataById(pointer, id.mostSignificantBits, id.leastSignificantBits)

    private external fun getFolderViewModeById(ptr: Long, idHigh: Long, idLow: Long): Char

    fun getFolderViewModeById(folderId: UUID) : Char = getFolderViewModeById(pointer, folderId.mostSignificantBits, folderId.leastSignificantBits)

    private external fun getFolderSortOptionById(ptr: Long, idHigh: Long, idLow: Long): Char

    fun getFolderSortOptionById(folderId: UUID) : Char = getFolderSortOptionById(pointer, folderId.mostSignificantBits, folderId.leastSignificantBits)

    private external fun downloadItem(ptr: Long, idHigh: Long, idLow: Long, filePath: String)

    /** Downloads a file for export. The file path must be a temporary path for downloading. If the file is already available offline, it is copied to the temporary path instead of being downloaded. */
    fun downloadItem(id: UUID, filePath: String) =
        downloadItem(pointer, id.mostSignificantBits, id.leastSignificantBits, filePath)

    private external fun getFolderMetadataById(ptr: Long, idHigh: Long, idLow: Long): FolderMetadata

    fun getFolderMetadataById(idHigh: Long, idLow: Long): FolderMetadata = getFolderMetadataById(pointer, idHigh, idLow)
    fun getFolderMetadataById(id: UUID): FolderMetadata = getFolderMetadataById(pointer, id.mostSignificantBits, id.leastSignificantBits)

    private external fun downloadThumbnail(ptr: Long, idHigh: Long, idLow: Long)
    fun downloadThumbnail(id: UUID) = downloadThumbnail(pointer, id.mostSignificantBits, id.leastSignificantBits)

    private external fun updateItem(ptr: Long, idHigh: Long, idLow: Long, itemAttributes: ItemAttributes)
    fun updateItem(id: UUID, itemAttributes: ItemAttributes) =
        updateItem(pointer, id.mostSignificantBits, id.leastSignificantBits, itemAttributes)

    private external fun renameItem(ptr: Long, idHigh: Long, idLow: Long, name: String)
    fun renameItem(id: UUID, name: String) = renameItem(pointer, id.mostSignificantBits, id.leastSignificantBits, name)

    private external fun refresh(ptr: Long)
    fun refresh() = refresh(pointer)

    private external fun sortItems(ptr: Long, option: Char, idHigh: Long, idLow: Long)
    
    fun sortItems(option: Char, id: UUID) {
        sortItems(pointer, option, id.mostSignificantBits, id.leastSignificantBits)
    }

    private external fun moveItem(
        ptr: Long,
        idHigh: Long,
        idLow: Long,
        parentIdHigh: Long,
        parentIdLow: Long,
    )

    fun moveItem(id: UUID, parentId: UUID) = moveItem(
        pointer,
        id.mostSignificantBits,
        id.leastSignificantBits,
        parentId.mostSignificantBits,
        parentId.leastSignificantBits,
    )

    private external fun softDeleteItem(ptr: Long, idHigh: Long, idLow: Long)
    fun softDeleteItem(id: UUID) = softDeleteItem(pointer, id.mostSignificantBits, id.leastSignificantBits)

    private external fun softDeleteItems(
        ptr: Long,
        idValues: LongArray,
        oldParentIdHigh: Long,
        oldParentIdLow: Long,
    )

    fun softDeleteItems(ids: List<UUID>, oldParentId: UUID) = softDeleteItems(
        pointer,
        ids.toIdValues(),
        oldParentId.mostSignificantBits,
        oldParentId.leastSignificantBits,
    )

    private external fun restoreItem(ptr: Long, idHigh: Long, idLow: Long)
    fun restoreItem(id: UUID) = restoreItem(pointer, id.mostSignificantBits, id.leastSignificantBits)

    private external fun restoreItems(ptr: Long, idValues: LongArray, oldParentIdHigh: Long,
                                      oldParentIdLow: Long,)
    fun restoreItems(ids: List<UUID>, oldParentId: UUID) = restoreItems(pointer, ids.toIdValues(), oldParentId.mostSignificantBits, oldParentId.leastSignificantBits)

    private external fun deleteItem(ptr: Long, idHigh: Long, idLow: Long)
    fun deleteItem(id: UUID) = deleteItem(pointer, id.mostSignificantBits, id.leastSignificantBits)

    private external fun deleteItems(
        ptr: Long,
        idValues: LongArray,
    )

    fun deleteItems(ids: List<UUID>) = deleteItems(
        pointer,
        ids.toIdValues()
    )

    private external fun moveItems(
        ptr: Long,
        idValues: LongArray,
        oldParentIdHigh: Long,
        oldParentIdLow: Long,
        parentIdHigh: Long,
        parentIdLow: Long,
    )

    fun moveItems(ids: List<UUID>, oldParentId: UUID, parentId: UUID) = moveItems(
        pointer,
        ids.toIdValues(),
        oldParentId.mostSignificantBits,
        oldParentId.leastSignificantBits,
        parentId.mostSignificantBits,
        parentId.leastSignificantBits,
    )

    private fun List<UUID>.toIdValues(): LongArray {
        val idValues = LongArray(size * 2)
        for (i in indices) {
            idValues[i * 2] = this[i].mostSignificantBits
            idValues[i * 2 + 1] = this[i].leastSignificantBits
        }
        return idValues
    }

    private external fun makeItemAvailableOffline(ptr: Long, idHigh: Long, idLow: Long)
    fun makeItemAvailableOffline(id: UUID) =
        makeItemAvailableOffline(pointer, id.mostSignificantBits, id.leastSignificantBits)

    private external fun makeItemOnlineOnly(ptr: Long, idHigh: Long, idLow: Long)
    fun makeItemOnlineOnly(id: UUID) =
        makeItemOnlineOnly(pointer, id.mostSignificantBits, id.leastSignificantBits)

}