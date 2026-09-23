package extra.blue.line.adsmanager

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.blue.line.R
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

@Keep
interface ADUnitType {
    var adUnitIDAM: Int?
    var adUnitIDFB: Int?
    var mediaAspectRatio: Int
    var adChoicesPlacement: Int
    var priority: AdsPriority
}

@Keep
interface BannerADUnit : ADUnitType {
    var adSizeAM: AdSize?
//    var adSizeFB: AdSizeFB?
}

@Keep
enum class AdsPriority {
    ADMOB,
    FACEBOOK,
    ADMOB_FACEBOOK,
    FACEBOOK_ADMOB
}


fun String.getPriorityRemotely() = kotlin.runCatching {
    val p = FirebaseRemoteConfig.getInstance().getLong(this).toInt()
    AdsPriority.values()[p]
}.getOrNull()

/**
 * you can declare this enum by your own app logic
 * if priority is different for same Ad-ID you must declare multiple properties e.g
 * */
@Keep
enum class ADUnitPlacements(
    @StringRes override var adUnitIDAM: Int? = null,
    @StringRes override var adUnitIDFB: Int? = null,
    override var mediaAspectRatio: Int = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE,
    override var adChoicesPlacement: Int = NativeAdOptions.ADCHOICES_TOP_RIGHT,
    override var priority: AdsPriority = AdsPriority.ADMOB // by default it will call ADMob only
) : ADUnitType
{
    SPLASH_INTER_AD(
        adUnitIDAM = R.string.splash_inter_ad_id,
    ),
    MAIN_SCREEN_INTER_AD(
        adUnitIDAM = R.string.main_inter_ad_id,
    ),
    LANGUAGE_NATIVE_AD(
        adUnitIDAM = R.string.language_native_ad_id,
    ),
    INTRUDER_NATIVE_AD(
        adUnitIDAM = R.string.intruder_native_ad_id,
    ),
    MAIN_NATIVE_AD(
        adUnitIDAM = R.string.main_native_ad_id,
    ),
    COMOFLAG_NATIVE_AD(
        adUnitIDAM = R.string.como_native_ad_id,
    ),
    THEME_NATIVE_AD(
        adUnitIDAM = R.string.theme_native_ad_id,
    ),
    ONBOARDING_NATIVE_AD(
        adUnitIDAM = R.string.onboarding_native_ad_id,
    ),
    LOCKSCREEN_NATIVE_AD(
        adUnitIDAM = R.string.lockscreen_native_ad_id,
    ),
//    COL_BANNER_HOME_AD(
//        adUnitIDAM = R.string.col_banner_am,
//    ),

}

enum class BannerPlacements(
    @StringRes override var adUnitIDAM: Int? = null,
    @StringRes override var adUnitIDFB: Int? = null,
    override var mediaAspectRatio: Int = NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE,
    override var adChoicesPlacement: Int = NativeAdOptions.ADCHOICES_TOP_RIGHT,
    override var priority: AdsPriority = AdsPriority.ADMOB, // by default it will call ADMob only
    override var adSizeAM: AdSize? = null, // by default it will adaptive banner
//    override var adSizeFB: AdSizeFB? = AdSizeFB.BANNER_HEIGHT_50
) : BannerADUnit {
    BANNER_HOME_AD(R.string.banner_am),
    BANNER_LOCK_AD(R.string.banner_lock),
//    BANNER_ALBUM_AD(R.string.banner_album),
    BANNER_ALBUM_IMAGES_AD(R.string.banner_album_images),
//    BANNER_TRASH_IMAGES_AD(R.string.banner_trash_images),
//    BANNER_MOVE_IMAGES_AD(R.string.banner_move_images),
//    BANNER_IMAGE_AD(R.string.banner_image_am)
}



fun Context.checkIfPremium() = TinyDB(this).getBoolean(getString(R.string.PREF_IS_SUBSCRIBED)) /*|| !isRewardedExpired()*/

//fun Context.getRewardedExpiryTime()= TinyDB(this).getLong(getString(R.string.reward_expiry_period),0)

fun Context.isRewardedExpired() : Boolean  {
//    if (getRewardedExpiryTime()>System.currentTimeMillis()){
        return false
//    }else{
//        TinyDB(this).putLong(getString(R.string.reward_expiry_period),-1)
//        return true
//    }
}


fun String.isEnabledRemotely(): Boolean {
    FirebaseRemoteConfig.getInstance().all.forEach {
//        Timber.e("all_values ${it.key} ${it.value.asString()}") // TODO check all values configured remotely
    }
    val value = FirebaseRemoteConfig.getInstance()
        .getBoolean(this)
//    Timber.e("key:$this value $value")
    return value
}