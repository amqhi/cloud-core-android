/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

import java.util.UUID

object SpecialFolder {
    val HOME = UUID(0x1111111111111111, 0x1111111111111111);
    val TRASH = UUID(0x1111111111111111, 0x1111111111111112)
    val UNKNOWN = UUID(0x0000000000000000, 0x0000000000000000)
}