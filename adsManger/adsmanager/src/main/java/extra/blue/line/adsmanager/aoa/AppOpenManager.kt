package extra.blue.line.adsmanager.aoa
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import extra.blue.line.adsmanager.aoa.base.BaseManager
import extra.blue.line.adsmanager.aoa.delay.DelayType
import extra.blue.line.adsmanager.aoa.delay.InitialDelay
import extra.blue.line.adsmanager.checkIfPremium
import extra.blue.line.adsmanager.isEnabledRemotely
import timber.log.Timber

/**
 * @AppOpenManager = A class that handles all of the App Open Ad operations.
 *
 * Constructor arguments:
 * @param application Required to keep a track of App's state.
 * @param adUnitId Pass your created AdUnitId
 * @param initialDelay for Initial Delay
 *
 * @param adRequest = Pass a customised AdRequest if you have any.
 * @see AdRequest
 *
 * @param orientation Ad's Orientation, Can be PORTRAIT or LANDSCAPE (Default is Portrait)
 * @see AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
 * @see AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE
 *
 */
open class AppOpenManager constructor(
    @NonNull application: Application,
    @NonNull initialDelay: InitialDelay,
    @NonNull var adUnitId: String,
    override var adRequest: AdRequest = AdRequest.Builder().build(),
//    override var orientation: Int = AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
    val totalShowCount: Int = 50,
    val remoteConfigKey: String? = null,
    @Nullable val onAdDismissed: (() -> Unit)? = null,
) : BaseManager(application),
    LifecycleObserver {

    var currentShowCount = 0
    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        this.initialDelay = initialDelay
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onStart() {
        if (initialDelay != InitialDelay.NONE) saveInitialDelayTime()
        Timber.e(" from AppOpenmanager On start")
//        showAdIfAvailable()
        Handler(Looper.getMainLooper()).postDelayed({showAdIfAvailable()},100)
    }

    // Let's fetch the Ad
    private fun fetchAd() {
        if (getApplication().checkIfPremium() ||  (remoteConfigKey != null && !remoteConfigKey.isEnabledRemotely()) || currentShowCount == totalShowCount || isAdAvailable()) return
        loadAd()
        Log.e("A pre-cached Ad was not available, loading one. AOA","A pre-cached Ad was not available, loading one. AOA")
    }

    // Show the Ad if the conditions are met.
    private fun showAdIfAvailable() {
        if (getApplication().checkIfPremium().not() &&currentShowCount != totalShowCount && !isShowingAd && isAdAvailable()
            && isInitialDelayOver() && currentActivity != null
        ) {
            appOpenAd?.fullScreenContentCallback =
                getFullScreenContentCallback {
                    currentShowCount++
//                    updateDelayTime() todo uncomment if you want to show each ad after delay
                }
//            val showAppOpen= FirebaseRemoteConfig.getInstance().getBoolean(SHOW_GRID_LINEAR_VIEW)
            appOpenAd?.show(currentActivity!!)
//            currentActivity!!.startWelcome()
        } else if (!isShowingAd) {
            Log.e("AOA ad not available $isShowingAd","AOA ad not available $isShowingAd")
            if (!isInitialDelayOver()) Log.e("AOA The Initial Delay period is not over yet.","AOA The Initial Delay period is not over yet")
            /**
             *If the next session happens after the delay period is over
             * & under 4 Hours, we can show a cached Ad.
             * However the above will only work for DelayType.HOURS.
             */
            if (initialDelay.delayPeriodType != DelayType.DAYS ||
                initialDelay.delayPeriodType == DelayType.DAYS &&
                isInitialDelayOver()
            ) fetchAd()
//            onAdDismissed?.invoke()
        }
    }

    private fun loadAd() {
        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(loadedAd: AppOpenAd) {
                this@AppOpenManager.appOpenAd = loadedAd
                this@AppOpenManager.loadTime = getCurrentTime()
                Log.e("Ad Loaded AOA","Ad Loaded AOA")
            }

            override fun onAdFailedToLoad(loadError: LoadAdError) {
//                onAdDismissed?.invoke()
                Log.e("AOA Ad Failed To Load, Reason: ${loadError.responseInfo}","AOA Ad Failed To Load, Reason: ${loadError.responseInfo}")
            }
        }
        runCatching {
            loadCallback?.let {
                AppOpenAd.load(getApplication(), adUnitId, adRequest, /*orientation,*/
                    it
                )
            }
        }
    }

    // Handling the visibility of App Open Ad
    private fun getFullScreenContentCallback(onAdShowed: (() -> Unit)?): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAd = false
                fetchAd()
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
//                onAdDismissed?.invoke()
                Log.e("AOA Ad Failed To Show Full-Screen Content: ${p0?.message}","AOA Ad Failed To Show Full-Screen Content: ${p0?.message}")
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAd = true
                onAdShowed?.invoke()
            }
        }
    }
}
