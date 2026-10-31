/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

import com.amqhi.cloud.core.Core.pointer
import java.util.UUID

class CachedState {

    private external fun save(ptr: Long)
    fun save() = save(pointer)
    private external fun getSortOption(ptr: Long, idHigh: Long, idLow: Long): Char

    fun getSortOption(folderId: UUID) : Char = getSortOption(pointer, folderId.mostSignificantBits, folderId.leastSignificantBits)

    private external fun getViewMode(ptr: Long, idHigh: Long, idLow: Long): Char

    fun getViewMode(folderId: UUID) : Char = getSortOption(pointer, folderId.mostSignificantBits, folderId.leastSignificantBits)

    private external fun getLastUpdateCheck(ptr: Long) : Long
    fun getLastUpdateCheck() : Long = getLastUpdateCheck(pointer)

    private external fun updateLastUpdateCheck(ptr: Long)
    fun updateLastUpdateCheck() = updateLastUpdateCheck(pointer)

}