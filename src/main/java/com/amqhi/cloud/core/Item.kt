/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

import java.util.UUID

data class Item(
    val id: UUID,
    val type: Char,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val eventAt: Long? = null,
    val deletedAt: Long? = null,
    val parentId: UUID,
    val name: String,
    val comment: String?,
    val encrypted: Boolean = false,
    val status: Char,
    val appScope: Short = 63
)