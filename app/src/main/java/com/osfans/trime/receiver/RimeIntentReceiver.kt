/*
 * SPDX-FileCopyrightText: 2015 - 2025 Rime community
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.osfans.trime.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.osfans.trime.BuildConfig
import com.osfans.trime.daemon.RimeDaemon
import com.osfans.trime.daemon.launchOnReady

class RimeIntentReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val rime = RimeDaemon.getFirstSessionOrNull() ?: return
        when (intent.action) {
            ACTION_DEPLOY -> {
                rime.launchOnReady { it.deploy() }
            }
            ACTION_SYNC_USER_DATA -> {
                rime.launchOnReady { it.syncUserData() }
            }
            else -> {}
        }
    }

    companion object {
        const val ACTION_DEPLOY = "${BuildConfig.APPLICATION_ID}.action.DEPLOY"
        const val ACTION_SYNC_USER_DATA = "${BuildConfig.APPLICATION_ID}.action.SYNC_USER_DATA"
    }
}
