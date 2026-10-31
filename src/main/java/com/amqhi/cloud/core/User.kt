/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

data class User(
    val localId: Char,
    val name: String,
    val email: String,
    val loginType: Short,
    val cachedPicturePath: String?
)
