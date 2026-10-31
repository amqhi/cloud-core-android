/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

object ItemStatus {
    const val AVAILABLE_OFFLINE: Char = 'a'
    const val ONLINE_ONLY: Char = 'o'
    const val PENDING_UPLOAD: Char = 'p'
    const val UPLOADING: Char = 'u'
}