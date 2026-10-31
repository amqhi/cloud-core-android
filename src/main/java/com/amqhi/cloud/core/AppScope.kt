/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

object AppScope {
    const val CLOUD: Short = 1
    const val NOTES: Short = 2
    const val MUSIC: Short = 4
    const val PHOTOS: Short = 8
    const val WEB: Short = 16
    const val AI: Short = 32

    const val CLOUD_INT: Int = 1
    const val NOTES_INT : Int = 2
    const val MUSIC_INT : Int = 4
    const val PHOTOS_INT : Int = 8
    const val WEB_INT : Int = 16
    const val AI_INT : Int = 32

    const val ALL: Short = 63
}