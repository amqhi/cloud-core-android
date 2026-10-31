/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

import java.util.UUID

data class ItemSummary(
    val id: UUID,
    val type: Char,
    val name: String,
    val thumbnailPath: String,
    val iconType: Char,
    val status: Char
)
