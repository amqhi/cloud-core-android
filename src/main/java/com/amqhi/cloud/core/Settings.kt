/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

import com.amqhi.cloud.core.Core.pointer

class Settings {
    private external fun save(ptr: Long)
    fun save() = save(pointer)
    private external fun getInstanceUrl(ptr: Long): String
    fun getInstanceUrl(): String = getInstanceUrl(pointer)

    private external fun setInstanceUrl(ptr: Long, instanceUrl: String)
    fun setInstanceUrl(instanceUrl: String) = setInstanceUrl(pointer, instanceUrl)

    private external fun getNavigationBarStyle(ptr: Long): Char
    fun getNavigationBarStyle(): Char = getNavigationBarStyle(pointer)

    private external fun setNavigationBarStyle(ptr: Long, style: Char)
    fun setNavigationBarStyle(style: Char) = setNavigationBarStyle(pointer, style)
}

object NavigationBarStyle {
    const val NORMAL = 'n'
    const val TRANSPARENT = 't'
}
