package extra.blue.line.adsmanager.events

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

fun Context.logEvent(eventName: String, vararg messages: String) {
    kotlin.runCatching {
        Timber.w("Send Firebase event called name:$eventName")
        val bundle = Bundle().apply {
            messages.forEachIndexed { x, parm ->
                putString(if (x == 0) eventName else "dev_param_$x", parm)
            }
        }
        FirebaseAnalytics.getInstance(this).logEvent(eventName, bundle)
    }.onFailure { Timber.e("on Event log $it") }
}

fun Context.setUserProperty(pair: Pair<String, String>) =
    FirebaseAnalytics.getInstance(this).setUserProperty(pair.first, pair.second)
