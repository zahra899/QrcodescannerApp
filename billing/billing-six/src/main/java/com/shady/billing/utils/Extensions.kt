package com.shady.billing.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi

fun List<String>.getOrEmpty(index: Int): String {
    return getOrElse(index) { "" }
}
@RequiresApi(Build.VERSION_CODES.M)
fun Activity.isOnlineCheck(): Boolean {
    var isOnline = false
    try {
        val manager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities: NetworkCapabilities?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            capabilities =
                manager.getNetworkCapabilities(manager.activeNetwork) // need ACCESS_NETWORK_STATE permission
        } else {
            var network: Network? = null
            for (n in manager.allNetworks.clone()) {
                if (manager.getNetworkInfo(n)!!.isConnected) {
                    network = n
                    break
                }
            }
            if (network == null) {
                return false
            }
            capabilities =
                manager.getNetworkCapabilities(network) // need ACCESS_NETWORK_STATE permission
        }
        isOnline =
            capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return isOnline
}