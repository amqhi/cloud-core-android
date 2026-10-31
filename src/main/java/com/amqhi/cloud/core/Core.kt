/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.amqhi.cloud.core

object Core {
    var pointer: Long = 0L
    lateinit var notifier: Notifier
    val itemManager = ItemManager()
    val cachedState = CachedState()
    val settings = Settings()
    lateinit var networkProvider: NetworkProvider

    init {
        System.loadLibrary("cloud_core")
    }

    fun initialize(
        appSupportPath: String,
        networkProvider: NetworkProvider,
        secureStorageProvider: SecureStorageProvider,
        notifier: Notifier,
        platformUtils: PlatformUtils
    ) {
        this.networkProvider = networkProvider
        this.notifier = notifier
        if (pointer != 0L) return
        pointer = initCore(appSupportPath, this.networkProvider, secureStorageProvider, notifier, platformUtils)
        initialize(pointer)
    }

    private external fun initCore(
        appSupportPath: String,
        networkProvider: NetworkProvider,
        securityProvider: SecureStorageProvider,
        notifier: Notifier,
        platformUtils: PlatformUtils
    ): Long

    private external fun initialize(ptr: Long)

    private external fun destroy(ptr: Long)

    fun destroy() = destroy(pointer)

    private external fun onSuspend(ptr: Long)
    fun onSuspend() = onSuspend(pointer)

    private external fun onResume(ptr: Long)
    fun onResume() = onResume(pointer)

    private external fun addUser(ptr: Long)
    fun addUser() = addUser(pointer)

    private external fun switchUser(ptr: Long, localId: Char)
    fun switchUser(localId: Char) = switchUser(pointer, localId)

    private external fun getUsers(ptr: Long): Array<User>
    fun getUsers(): Array<User> = getUsers(pointer)

    private external fun getUnselectedUsers(ptr: Long): Array<User>
    fun getUnselectedUsers(): Array<User> = getUnselectedUsers(pointer)

    private external fun getSelectedUser(ptr: Long): User
    fun getSelectedUser(): User = getSelectedUser(pointer)

    private external fun handleLoginOnFirstRun(ptr: Long, instanceUrl: String, email: String, password: String)
    fun handleLogin(instanceUrl: String, email: String, password: String) = handleLoginOnFirstRun(pointer, instanceUrl, email, password)

    private external fun handleLogin(ptr: Long, email: String, password: String)
    fun handleLogin(email: String, password: String) = handleLogin(pointer, email, password)

    private external fun handleLogout(ptr: Long)
    fun handleLogout() = handleLogout(pointer)

    private external fun handleRegisterOnFirstRun(ptr: Long, instanceUrl: String,  email: String, password: String, name: String)
    fun handleRegister(instanceUrl: String, email: String, name: String, password: String) =
        handleRegisterOnFirstRun(pointer, instanceUrl, email, password, name)

    private external fun handleRegister(ptr: Long, email: String, password: String, name: String)
    fun handleRegister(email: String, name: String, password: String) =
        handleRegister(pointer, email, password, name)

    private external fun onNetworkReconnect(ptr: Long)
    fun onNetworkReconnect() = onNetworkReconnect(pointer)

}