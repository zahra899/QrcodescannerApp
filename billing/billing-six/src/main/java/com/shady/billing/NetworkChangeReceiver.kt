package com.shady.billing

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager


class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        try {
            if (isOnline(context).also { println("NetworkChangeReceiver, isOnline: $it") }) {
                BillingManager.getInstance()
                    .startConnection()
            }
        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun isOnline(context: Context): Boolean {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected)
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return false
        }
    }

}