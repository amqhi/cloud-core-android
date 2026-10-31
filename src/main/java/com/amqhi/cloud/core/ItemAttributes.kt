/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

data class ItemAttributes(
    val name: String,
    val eventAt: Long? = null,
    val parentIdHigh: Long,
    val parentIdLow: Long,
    val encrypted: Boolean = false,
    val comment: String? = null,
    val appScope: Short = AppScope.ALL
)
