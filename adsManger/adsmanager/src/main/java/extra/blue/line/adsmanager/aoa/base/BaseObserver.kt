package extra.blue.line.adsmanager.aoa.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * Base Observer class to identify the Current visible Activity
 * @param application Required to register Activity Lifecycle Callbacks
 *
 * We only need onActivityStarted, onActivityResumed & onActivityDestroyed
 * to track the Current visible Activity & therefore it was meaningless
 * to add all those abstract methods in a single class.
 *
 * Extended ahead by:
 * @see BaseManager
 */
open class BaseObserver(application: Application) : Application.ActivityLifecycleCallbacks {
    init {
        // Cannot directly use `this`
        // Issue : Leaking 'this' in constructor of non-final class BaseObserver
        registerActivityLifecycleCallbacks(application)
    }

    protected var currentActivity: Activity? = null
    private val adActivityList: MutableList<WeakReference<Any>> = mutableListOf()
    private fun registerActivityLifecycleCallbacks(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
//        currentActivity = activity.takeIf { it.cast<AOAListener>() != null }
        currentActivity = if (adActivityList.size>0){
            null
        }else{
            activity.takeIf { current ->
                current.cast<AOAListener>() != null
            }
        }
        Timber.e("onActivityStarted $activity-->${adActivityList.size}")
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = if (adActivityList.size>0){
            null
        }else{
            activity.takeIf { current ->
                current.cast<AOAListener>() != null
            }
        }
//        currentActivity = activity.takeIf { it.cast<AOAListener>() != null }
//        currentActivity = null
        Timber.e("onActivityResumed $activity-->${adActivityList.size}")
    }

    override fun onActivityDestroyed(activity: Activity) {
        adActivityList.removeAll { it.get() == activity }
        Timber.e("onActivityDestroyed $activity-->${adActivityList.size}")
        currentActivity = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//        currentActivity = activity//.cast()
//        currentActivity = activity.takeIf { it.cast<AOAListener>() != null }
        /*if (activity.cast<AOAListener>() == null) {
            adActivityList.add(WeakReference(activity))
        }*/
        currentActivity = if (adActivityList.size>0){
            null
        }else{
            activity.takeIf { current ->
                current.cast<AOAListener>() != null
            }
        }

        Timber.e("onActivityCreated $activity-->${adActivityList.size}")
        Timber.e("onActivityCreated1111 ${(activity) !in adActivityList.mapNotNull { it.get() }}")
    }

    override fun onActivityPaused(activity: Activity) {
        currentActivity = null
        Timber.e("onActivityPaused $activity")
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}

inline fun <reified T : Any> Any.cast() = this as? T

interface AOAListener