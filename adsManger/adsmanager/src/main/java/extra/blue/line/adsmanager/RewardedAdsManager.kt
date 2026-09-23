package com.blue.line.adsmanager

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import extra.blue.line.adsmanager.*
import timber.log.Timber
import java.util.*


@Keep
fun Context.loadRewardedInterstitialAd(
    type: String? = null,
    ADUnit: ADUnitType, reloadOnClosed: Boolean = false,
    onLoaded: ((InterAdPair) -> Unit)? = null, onClosed: (() -> Unit)? = null,onFailed: (() -> Unit)? = null,
    remoteConfigKey: String? = null
) {
    if (checkIfPremium() || (remoteConfigKey != null && !remoteConfigKey.isEnabledRemotely())) {
        return
    }
    Timber.e("load rewarded inter priority ${ADUnit.priority}")
    when (ADUnit.priority) {
        AdsPriority.ADMOB/*, AdsPriority.ADMOB_FACEBOOK*/ ->
            newAMRewardedInterstitialAd(type=type,ADUnit, reloadOnClosed, onLoaded, onClosed,onFailed)
        else -> {}
    }
}

fun Context.newAMRewardedInterstitialAd(
    type: String? = null,
    ADUnit: ADUnitType, reloadOnClosed: Boolean,
    onLoaded: ((InterAdPair) -> Unit)?, onClosed: (() -> Unit)?, onFailed: (() -> Unit)?
) {
    fun getContentCallback() = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Timber.e("rewarded inter AM Ad was dismissed.")

            onClosed?.invoke()
            if (reloadOnClosed)
                loadRewardedInterstitialAd(type=type,ADUnit, reloadOnClosed, onLoaded, onClosed,onFailed)
        }

        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
            Timber.e("rewarded Inter AM Ad failed to show.")
            // Don't forget to set the ad reference to null so you
            // don't show the ad a second time.
//                mInterstitialAd = null
            if (reloadOnClosed)
                loadInterstitialAd(type=type,ADUnit, reloadOnClosed, onLoaded, onClosed)
        }

        override fun onAdShowedFullScreenContent() {
            Timber.e("rewarded inter AM Ad showed fullscreen content.")
            // Called when ad is dismissed.
        }
    }
//
    val testDeviceIds = listOf("FC8439F712FE56F84D71DE85B1247AAA","23006130FA712B2F93DADB31EC729CCD","92E0D13FEF33E4937AD6E5022D275207","006C250F03FE5FD90EAA758D6A1967FF")
    val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
    MobileAds.setRequestConfiguration(configuration)
//

    val adUnitID = ADUnit.adUnitIDAM?.let { this.getString(it) }


   val adReq= AdRequest.Builder().build()

//    ca-app-pub-5363442273143131/5635623061

//    if (BuildConfig.DEBUG && !adReq.isTestDevice(this)){
//        onFailed?.invoke()
//        return
//    }


    if (adUnitID != null) {
        RewardedInterstitialAd.load(
            this, adUnitID, adReq,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Timber.e("onFailed rewarded Inter AM ${p0.message}      ${p0.code}")
                    /*if (ADUnit.priority == AdsPriority.ADMOB_FACEBOOK)
                    newFBInterstitial(ADUnit, reloadOnClosed, onLoaded, onClosed)*/
                    onFailed?.invoke()
                }

                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    Timber.e("onAdLoaded rewarded Inter AM")
                    ad.fullScreenContentCallback = getContentCallback()
                    onLoaded?.invoke(InterAdPair(rewardedInterAM = ad))




                }
            })
    }else{
        onFailed?.invoke()
    }

}
@JvmOverloads
fun RewardedInterstitialAd.showAd(activity: Activity, onRewardEarned: OnUserEarnedRewardListener) =
    show(activity, onRewardEarned )



fun Context.loadRewardedVideoAd(ADUnit: ADUnitType, onLoaded: (ad: RewardedAd) -> Unit, onFailed: (() -> Unit)? = null) {
    val adId = ADUnit.adUnitIDAM?.let { getString(it) }

    if (adId == null) {
        onFailed?.invoke()
        return
    }
    Log.e("mytag", "RewardedVideoAd >>> calling", )
    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(this, adId, adRequest, object : RewardedAdLoadCallback() {
        override fun onAdFailedToLoad(error: LoadAdError) {
            onFailed?.invoke()
            Timber.e("RewardedVideoAd >>> onFailed, error: ${error.message}")
            Log.e("mytag", "RewardedVideoAd >>> onFailed", )

        }

        override fun onAdLoaded(ad: RewardedAd) {
            onLoaded(ad)
            Timber.w("RewardedVideoAd >>> onLoaded")
            Log.e("mytag", "RewardedVideoAd >>> onLoaded", )
        }
    })
}


fun RewardedAd.showAd(activity: Activity, onDismiss: (reward: RewardItem?, error: AdError?) -> Unit) {
    var rewardItem: RewardItem? = null
    fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            onDismiss(rewardItem, null)

        }

        override fun onAdFailedToShowFullScreenContent(error: AdError) {
            onDismiss(rewardItem, error)
        }
    }
    show(activity) { rewardItem = it }
}
