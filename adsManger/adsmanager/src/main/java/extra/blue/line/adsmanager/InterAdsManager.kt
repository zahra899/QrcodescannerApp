package extra.blue.line.adsmanager

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import com.blue.line.adsmanager.showAd

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import timber.log.Timber
import java.util.*
//import com.facebook.ads.InterstitialAd as InterstitialAdFB

/*@Keep
data class InterAdPair(var interAM: InterstitialAd? = null*//*, var interFB: InterstitialAdFB? =null*//*) {
    fun showAd(context: Activity, isDelayEnabled: Boolean = false): Boolean = when {
        context.checkIfPremium() -> false
        interAM!=null*//* || interFB?.isAdLoaded*//* == true -> {
            Timber.e("asdf->4>${interAM}--${InterDelayTimer.isDelaySpent()}--${context}")
            if (!isDelayEnabled || InterDelayTimer.isDelaySpent()) interAM?.showAd(context)
                *//*?: interFB?.show()*//*
            true
        }
        else ->{
            Timber.e("asdf->5>")
            false
        }
    }
    fun isLoaded() = interAM!=null *//*|| interFB?.isAdLoaded == true*//*
}*/
@Keep
data class InterAdPair(var interAM: InterstitialAd? = null, var interFB: InterstitialAd? = null,var rewardedInterAM: RewardedInterstitialAd?=null) {
    fun showAd(context: Activity, isDelayEnabled: Boolean = false,onRewardEarned: OnUserEarnedRewardListener?=null): Boolean {

        val isShow = when {
            context.checkIfPremium() -> false
            interAM != null /*|| interFB?.isAdLoaded == true*/ -> {
                if (!isDelayEnabled || InterDelayTimer.isDelaySpent()) interAM?.showAd(context)
                /*?: interFB?.show()*/
                true
            }
            /*     interFB?.isAdLoaded == true -> {
                     context.logEvent("ad_impr_inter_FB")
                     interFB?.show();
                     true
                 }*/
            rewardedInterAM != null && onRewardEarned !=null->{
                rewardedInterAM!!.showAd(context,onRewardEarned)
                true
            }
            else -> false
        }
//        context.logEvent("inter_show", "$isShow")
        return isShow
    }

    fun isLoaded() = interAM != null /*|| interFB?.isAdLoaded == true*/
}



@Keep

object InterDelayTimer {
    val currentTime1 = Calendar.getInstance().timeInMillis
    var lastShowTimeInMillis = currentTime1
    const val INTERSTITIAL_DELAY_TIME = SHOW_DELAY_TIMER_TIME
    fun isDelaySpent(a:Boolean?=null): Boolean {
        val currentTime = Calendar.getInstance().timeInMillis
        val diff = (currentTime - lastShowTimeInMillis) / 1000L
        Log.e("ShowThecalculatetime","lastShowTimeInMillis : $lastShowTimeInMillis current time : $currentTime diff : $diff ")
        val requiredDelay =
            FirebaseRemoteConfig.getInstance().getLong(INTERSTITIAL_DELAY_TIME)
        return if (diff >= requiredDelay) {
            if (a!=null && a==false)
                lastShowTimeInMillis = currentTime
            true
        } else false
    }
}

@Keep
fun Context.loadInterstitialAd(
    type: String? = null,
    ADUnit: ADUnitType,
    reloadOnClosed: Boolean = false,
    onLoaded: ((InterAdPair) -> Unit)? = null,
    onClosed: (() -> Unit)? = null,
    onFailed: (() -> Unit)? = null,
    remoteConfigKey: String? = null
) {
    if (checkIfPremium() || (remoteConfigKey != null && !remoteConfigKey.isEnabledRemotely())) {
        Log.d("CheckSplashInterStatus","---->${checkIfPremium() || (remoteConfigKey != null && !remoteConfigKey.isEnabledRemotely())}")
        onFailed?.invoke()
        return
    }
    Log.e("","onFailed Inter AM 4")
    Timber.e("load inter priority ${ADUnit.priority}")
    when (ADUnit.priority) {
        AdsPriority.ADMOB/*, AdsPriority.ADMOB_FACEBOOK*/ ->
            newAMInterstitialAd(type,ADUnit, reloadOnClosed, onLoaded, onClosed, onFailed)
       /* AdsPriority.FACEBOOK*//*, AdsPriority.FACEBOOK_ADMOB*//* ->
            newFBInterstitial(ADUnit, reloadOnClosed, onLoaded, onClosed,onFailed)*/
        else -> {}
    }
}

fun Context.newAMInterstitialAd(
    type: String? = null,
    ADUnit: ADUnitType, reloadOnClosed: Boolean,
    onLoaded: ((InterAdPair) -> Unit)?, onClosed: (() -> Unit)?, onFailed: (() -> Unit)?
) {

    fun getContentCallback() = object : FullScreenContentCallback() {

        override fun onAdDismissedFullScreenContent() {
            Log.e("","onFailed Inter AM 3")
            Timber.e("inter AM Ad was dismissed.")

            // Don't forget to set the ad reference to null so you
            // don't show the ad a second time.
//                mInterstitialAd = null
//                loadAd()
            Log.e("TAG", "$type : closed", )
            onClosed?.invoke()
            if (reloadOnClosed)
                loadInterstitialAd(type, ADUnit, reloadOnClosed, onLoaded, onClosed,onFailed)
        }

//        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError?) {
//            Timber.e("Inter AM Ad failed to show.")
//            // Don't forget to set the ad reference to null so you
//            // don't show the ad a second time.
////                mInterstitialAd = null
//            if (reloadOnClosed)
//                loadInterstitialAd(ADUnit, reloadOnClosed, onLoaded, onClosed)
//        }

        override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {

            Timber.e("Inter AM Ad failed to show.")
            // Don't forget to set the ad reference to null so you
            // don't show the ad a second time.
//                mInterstitialAd = null
            Log.e("","onFailed Inter AM 8")
            if (reloadOnClosed)
                loadInterstitialAd(type,ADUnit, reloadOnClosed, onLoaded, onClosed)
        }

        override fun onAdShowedFullScreenContent() {
            Timber.e("inter AM Ad showed fullscreen content.")

            // Called when ad is dismissed.
        }
    }

    Log.e("","onFailed Inter AM 2")
    val adUnitID = ADUnit.adUnitIDAM?.let { this.getString(it) }

    if (adUnitID != null) {

        runCatching {

            Log.e("","onFailed Inter AM 1")
            Log.e("TAG", "$type : calling", )
            InterstitialAd.load(
                this, adUnitID, AdRequest.Builder().build(),

                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        Log.d("CheckSplashInterStatus","---->${p0}}")

                        Log.e("TAG", "$type : failed", )

                        /*  if (ADUnit.priority == AdsPriority.ADMOB_FACEBOOK)
                              newFBInterstitial(ADUnit, reloadOnClosed, onLoaded, onClosed, onFailed)*/
                        onFailed?.invoke()
                    }


                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d("CheckSplashInterStatus","---->$ad}")
                        ad.fullScreenContentCallback = getContentCallback()
                        Log.e("TAG", "$type : loaded", )
                        Log.e("axim", "Ads Load call succes : loaded", )

                        onLoaded?.invoke(InterAdPair(interAM = ad))
                    }
                })
            }

    }

}

@JvmOverloads
fun InterstitialAd.showAd(activity: Activity? = null) =
    runCatching{
        activity?.let {
            Timber.e("asdf->6>")
            show(it) }
    }



/*fun Context.newFBInterstitial(
    ADUnit: ADUnitType, reloadOnClosed: Boolean,
    onLoaded: ((InterAdPair) -> Unit)?, onClosed: (() -> Unit)?, onFailed: (() -> Unit)?
): InterstitialAdFB? {
    val interstitialAd:InterstitialAdFB? = ADUnit.adUnitIDFB?.let { id -> InterstitialAdFB(this, getString(id)) }
    Log.e("InterAd","InterAdFB 1"+interstitialAd?.placementId)
    interstitialAd?.run {
        loadAd(
            buildLoadAdConfig()?.withAdListener(object :
                InterstitialAdListener {

                override fun onInterstitialDismissed(p0: Ad?) {
                    onClosed?.invoke()
                    if (reloadOnClosed)
                        loadInterstitialAd(ADUnit, reloadOnClosed, onLoaded, onClosed)
                }

                override fun onError(p0: Ad?, p1: AdError?) {
                    Log.e("InterAd","InterAdFB error  ${p1?.errorMessage} ${p1?.errorCode}")
                    Timber.e("onFailed Inter FB ${p1?.errorMessage} ${p1?.errorCode}")
                    if (ADUnit.priority == AdsPriority.FACEBOOK_ADMOB)
                        newAMInterstitialAd(ADUnit, reloadOnClosed, onLoaded, onClosed,null)
                    onFailed?.invoke()
                }

                override fun onAdLoaded(p0: Ad?): Unit {
                    Log.e("InterAd","InterAdFB loaded")
                    onLoaded?.invoke(InterAdPair(interFB = interstitialAd))
                }


                override fun onAdClicked(p0: Ad?) {
                }

                override fun onLoggingImpression(p0: Ad?) {
                }

                override fun onInterstitialDisplayed(p0: Ad?) {

                }

            })?.build()
        )
    }
    return interstitialAd
}*/

/*
open class FBInterAdListener : InterstitialAdListener {
    override fun onInterstitialDisplayed(p0: Ad?) {}

    override fun onAdClicked(p0: Ad?) {}

    override fun onInterstitialDismissed(p0: Ad?) {}

    override fun onError(p0: Ad?, p1: AdError?) {}

    override fun onAdLoaded(p0: Ad?) {}

    override fun onLoggingImpression(p0: Ad?) {}

}*/
