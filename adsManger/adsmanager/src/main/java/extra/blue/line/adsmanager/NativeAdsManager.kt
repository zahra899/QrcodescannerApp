package com.smart.lines.adsmanager

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import extra.blue.line.adsmanager.ADUnitType
import extra.blue.line.adsmanager.AdsPriority
import extra.blue.line.adsmanager.checkIfPremium
import extra.blue.line.adsmanager.events.logEvent
import extra.blue.line.adsmanager.isEnabledRemotely
import timber.log.Timber
@Keep
data class NativeAdPair(var nativeAM: NativeAd? = null /*var nativeFB: NativeAd? = null*/) {
    fun populate(context: Context, @LayoutRes adLayout: Int, frameLayout: FrameLayout?) {
        when {
            context.checkIfPremium() -> return
            nativeAM != null -> {
                context.inflateUnifiedAd(adLayout)?.let { layout ->
                    kotlin.runCatching {
                        frameLayout?.let /*post*/ {
                            frameLayout.removeAllViews()
                            nativeAM!!.populateNativeAdView(layout)
                            frameLayout.addView(layout)
                            frameLayout.visibility = View.VISIBLE
                        }
                    }.onFailure {
                        frameLayout?.post {
                            frameLayout.removeAllViews()
                            nativeAM!!.populateNativeAdView(layout)
                            frameLayout.addView(layout)
                            frameLayout.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
    fun isLoaded() = nativeAM != null /*|| nativeFB != null*/
}
@JvmOverloads
fun Context.loadNativeAd(
    frameLayout: FrameLayout?,
    ADUnit: ADUnitType,
    @LayoutRes adLayout: Int,
    isShimmer: Boolean? = false,
    remoteConfigKey: String? = null,
    type: String? = null,
    value: Int? = null,
    AMCallback: ((NativeAd) -> Unit)? = null,
    AMCallbackMultiple: ((NativeAd,Boolean?) -> Unit)? = null,
    FBCallback: (() -> Unit)? = null,
    onError: (() -> Unit)? = null
) {

    if (checkIfPremium() ) {
        frameLayout?.visibility = View.GONE
        frameLayout?.postDelayed({ onError?.invoke()
            Log.e("TAG", "loadNativeAd ${type} Remoteconfige false", )
        }, 300)

        return
    }else if ((remoteConfigKey != null && !remoteConfigKey.isEnabledRemotely())){
        frameLayout?.visibility = View.GONE
        frameLayout?.postDelayed({ onError?.invoke()

        }, 300)

        return
    }

    when (ADUnit.priority) {
        AdsPriority.ADMOB ,AdsPriority.ADMOB_FACEBOOK ->
            loadNativeAM(frameLayout, adLayout, ADUnit, AMCallback, FBCallback, onError,type,value,AMCallbackMultiple)


        else -> {}
    }

}


private fun Context.loadNativeAM(
    frameLayout: FrameLayout?, adLayout: Int, ADUnit: ADUnitType,
    AMSuccessCallBack: ((NativeAd) -> Unit)? = null,
    FBCallback: ((/*NativeAdFB*/) -> Unit)? = null,
    onError: (() -> Unit)? = null,
    type: String? = null,
    value:Int?=null,AMCallbackMultiple: ((NativeAd,Boolean?) -> Unit)? = null,
) {
    println("Native Ad Calling Loading")
    var adLoader:AdLoader? = null
    val builder =
        ADUnit.adUnitIDAM?.let { getString(it) }?.let {
            AdLoader.Builder(this, it).withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setMediaAspectRatio(ADUnit.mediaAspectRatio)
                    .setAdChoicesPlacement(ADUnit.adChoicesPlacement)
                    .build()
            )
        }
   adLoader =
       builder?.forNativeAd { ad ->
            inflateUnifiedAd(adLayout)?.let { adLayout ->

                Log.e("add", "$type : loeded", )
                frameLayout?.post {
                    frameLayout.removeAllViews()
                    ad.populateNativeAdView(adLayout)
                    frameLayout.addView(adLayout)
                    frameLayout.visibility = View.VISIBLE
                    logEvent("ad_impr_native_AM")
                }
            }
            if (adLoader?.isLoading == true) {
                // The AdLoader is still loading ads.
                Log.e("add", "$type : loading true", )



                // Expect more adLoaded or onAdFailedToLoad callbacks.
            } else {
                Timber.e("nativeLoading--->>>false")
            }
            AMSuccessCallBack?.invoke(ad)
        }?.withAdListener(
            object : AdListener() {

                override fun onAdLoaded() {
                    super.onAdLoaded()

                }

                override fun onAdFailedToLoad(errorCode: LoadAdError) {

                  /*  if (ADUnit.priority == AdsPriority.ADMOB_FACEBOOK)
                        loadNativeFB(frameLayout, adLayout, ADUnit, FBCallback, onError = onError)
                    else {*/
                        onError?.invoke()

                        frameLayout?.removeAllViews()
//                    }
                    Log.e("add", "$type : Failed", )

                }

                override fun onAdClosed() {
                    println("Native Ad Calling Loading Closed")
                    Timber.e("onAdClosed ")
                    Log.e("add", "$type : Closed", )
                }

            })?.apply {
           if (value!=null && value>1){
               forNativeAd {
                   Timber.e("nativeLoading--->>>true")
                   Log.e("add", "$type : Multi laod", )
                   Log.e("axim", "loadNativeAM: is laoding test ${adLoader?.isLoading }", )
                   AMCallbackMultiple?.invoke(it,adLoader?.isLoading)
                   if (adLoader?.isLoading == true) {

                       // The AdLoader is still loading ads.
                       // Expect more adLoaded or onAdFailedToLoad callbacks.
                   } else {
                       // The AdLoader has finished loading ads.
                   }
               }
           }

       }?.build()
    kotlin.runCatching {
        Log.e("add", "$type : calling", )
        value?.let { adLoader?.loadAds(AdRequest.Builder().build(), it) }
    }



}

fun Context.inflateUnifiedAd(adLayout: Int): NativeAdView? = if (adLayout == -1) null else
    (this as? Activity)?.layoutInflater?.inflate(adLayout, null)
        ?.let {
            NativeAdView(this).apply {

                addView(it)
            }
        }


fun NativeAd.populateNativeAdView(adView: NativeAdView) {

    val ad_media=  adView.findViewById<com.google.android.gms.ads.nativead.MediaView>(com.blue.line.R.id.ad_media)
    val ad_headline=  adView.findViewById<TextView>(com.blue.line.R.id.ad_headline)
    val ad_body=  adView.findViewById<TextView>(com.blue.line.R.id.ad_body)
    val ad_call_to_action=  adView.findViewById<Button>(com.blue.line.R.id.ad_call_to_action)
    val ad_icon=  adView.findViewById<ImageView>(com.blue.line.R.id.ad_icon)


    ad_media?.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
    ad_media?.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
        override fun onChildViewRemoved(p0: View?, p1: View?) = Unit

        override fun onChildViewAdded(parent: View?, child: View?) {
            if (child is ImageView) {
                child.adjustViewBounds = true
                child.scaleType = ImageView.ScaleType.CENTER_CROP
                child.clipToOutline=true
            }
            Timber.w("parent ${parent?.javaClass} child ${child?.javaClass}")
        }
    })
    if (ad_media !=null){
        adView.mediaView = ad_media
        ad_media.visibility = View.VISIBLE
    }

//    adView.ad_media_fb?.visibility = View.GONE
//    adView.ad_icon_fb?.visibility = View.GONE

    // Register the view used for each individual asset.
    adView.headlineView = ad_headline
    adView.bodyView = ad_body
    adView.callToActionView = ad_call_to_action
    ad_icon?.clipToOutline=true
    adView.iconView = ad_icon   // cmnt by axim

//    adView.ad_icon_fb?.visibility = View.GONE

//    adView.advertiserView = adView.ad_advertiser

    // Some assets are guaranteed to be in every UnifiedNativeAd.
    (adView.headlineView as? TextView)?.text = this.headline
    (adView.bodyView as? TextView)?.text = this.body
    (adView.callToActionView as? TextView)?.text = this.callToAction

    // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
    // check before trying to display them.

    if (this.icon == null) {
        adView.iconView?.visibility = View.GONE
    } else {
        (adView.iconView as? ImageView)?.setImageDrawable(this.icon!!.drawable)
        adView.iconView?.visibility = View.VISIBLE
    }


    adView.bodyView?.visibility = if (this.body.isNullOrEmpty()) View.GONE else View.VISIBLE
    (adView.bodyView as? TextView?)?.text = this.body

    if (this.advertiser == null) {
        adView.advertiserView?.visibility = View.GONE
    } else {
        (adView.advertiserView as? TextView)?.text = this.advertiser
        adView.advertiserView?.visibility = View.VISIBLE
    }
    // Assign native ad object to the native view.
    adView.setNativeAd(this)
}


