package extra.blue.line.adsmanager

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import extra.blue.line.adsmanager.events.logEvent

class AdmobBannerAds {

    private var adaptiveAdView: AdView? = null

    private val AD_TAG = "Collapsable Banner->"

    @SuppressLint("VisibleForTests")
    fun loadBannerAds(
        activity: Activity?,
        adsPlaceHolder: FrameLayout,
        ADUnit: ADUnitType,
       /* remoteConfigKey: String? = null,*/
        collapsiblePositionType: CollapsiblePositionType = CollapsiblePositionType.NONE,
        bannerCallBack: BannerCallBack
    ) {
        activity?.let { mActivity ->
            try {
                if (mActivity.checkIfPremium() /*|| (remoteConfigKey != null && !remoteConfigKey.isEnabledRemotely())*/) {
                    adsPlaceHolder.visibility = View.GONE
                    adsPlaceHolder.postDelayed({ bannerCallBack.onAdFailedToLoad("Remote Config or premium") }, 300)
                    return
                }
                val admobAdaptiveIds=ADUnit.adUnitIDAM?.let { mActivity.getString(it) }
                if ( admobAdaptiveIds?.isNotEmpty()==true) {
                    adsPlaceHolder.visibility = View.VISIBLE
                    adaptiveAdView = AdView(mActivity)
                    adaptiveAdView?.adUnitId = admobAdaptiveIds
                    adaptiveAdView?.setAdSize(getAdSize(mActivity, adsPlaceHolder))

                    val adRequest: AdRequest = when (collapsiblePositionType) {
                        CollapsiblePositionType.NONE -> {
                            AdRequest
                                .Builder()
                                .build()
                        }
                        CollapsiblePositionType.BOTTOM -> {
                            AdRequest
                                .Builder()
                                .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                                    putString("collapsible", "bottom")
                                })
                                .build()
                        }
                        CollapsiblePositionType.TOP -> {
                            AdRequest
                                .Builder()
                                .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
                                    putString("collapsible", "top")
                                })
                                .build()
                        }
                    }

                    adaptiveAdView?.loadAd(adRequest)
                    adaptiveAdView?.adListener = object : AdListener() {
                        override fun onAdLoaded() {

                            Log.d(AD_TAG, "-->AdLoaded")
                            displayBannerAd(adsPlaceHolder)
                            bannerCallBack.onAdLoaded()
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            Log.e(AD_TAG, "-->AdFailedToLoad ${adError}")
                            adsPlaceHolder.visibility = View.GONE
                            bannerCallBack.onAdFailedToLoad(adError.message)
                        }

                        override fun onAdImpression() {
//                            Log.d(AD_TAG, "admob banner onAdImpression")
                            mActivity.logEvent("ad_impr_collapsable_banner_AM")
                            bannerCallBack.onAdImpression()
                            super.onAdImpression()
                        }

                        override fun onAdClicked() {
//                            Log.d(AD_TAG, "admob banner onAdClicked")
                            bannerCallBack.onAdClicked()
                            super.onAdClicked()
                        }

                        override fun onAdClosed() {
//                            Log.d(AD_TAG, "admob banner onAdClosed")
                            bannerCallBack.onAdClosed()
                            super.onAdClosed()
                        }

                        override fun onAdOpened() {
                            Log.d(AD_TAG, "-->AdOpened")
                            bannerCallBack.onAdOpened()
                            super.onAdOpened()
                        }

                        override fun onAdSwipeGestureClicked() {
//                            Log.d(AD_TAG, "admob banner onAdSwipeGestureClicked")
                            bannerCallBack.onAdSwipeGestureClicked()
                            super.onAdSwipeGestureClicked()
                        }
                    }
                } else {
                    adsPlaceHolder.removeAllViews()
                    adsPlaceHolder.visibility = View.GONE
                    bannerCallBack.onAdFailedToLoad("Ad Unit Id Invalid")

                }
            } catch (ex: Exception) {
                adsPlaceHolder.removeAllViews()
                adsPlaceHolder.visibility = View.GONE
                Log.e(AD_TAG, "${ex.message}")
                bannerCallBack.onAdFailedToLoad("${ex.message}")
            }
        }

    }

    private fun displayBannerAd(adsPlaceHolder: FrameLayout) {
        try {
            if (adaptiveAdView != null) {
                val viewGroup: ViewGroup? = adaptiveAdView?.parent as ViewGroup?
                viewGroup?.removeView(adaptiveAdView)

                adsPlaceHolder.removeAllViews()
                adsPlaceHolder.addView(adaptiveAdView)
            } else {
                adsPlaceHolder.removeAllViews()
                adsPlaceHolder.visibility = View.GONE
            }
        } catch (ex: Exception) {
            Log.e(AD_TAG, "inflateBannerAd: ${ex.message}")
        }

    }

    fun bannerOnPause() {
        try {
            adaptiveAdView?.pause()
        } catch (ex: Exception) {
            Log.e(AD_TAG, "bannerOnPause: ${ex.message}")
        }

    }

    fun bannerOnResume() {
        try {
            adaptiveAdView?.resume()
        } catch (ex: Exception) {
            Log.e(AD_TAG, "bannerOnPause: ${ex.message}")
        }
    }

    fun bannerOnDestroy() {
        try {
            adaptiveAdView?.destroy()
            adaptiveAdView = null
        } catch (ex: Exception) {
            Log.e(AD_TAG, "bannerOnPause: ${ex.message}")
        }
    }


    private fun getAdSize(mActivity: Activity, adContainer: FrameLayout): AdSize {
        val display = mActivity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = adContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth)
    }


}
enum class CollapsiblePositionType {
    TOP,
    BOTTOM,
    NONE
}
interface BannerCallBack {
    fun onAdFailedToLoad(adError:String){}
    fun onAdLoaded(){}
    fun onAdImpression(){}
    fun onPreloaded(){}
    fun onAdClicked(){}
    fun onAdClosed(){}
    fun onAdOpened(){}
    fun onAdSwipeGestureClicked(){}
}