package extra.blue.line.adsmanager

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
object RemoteConfig {

    private const val FETCH_TIME_INTERVAL = 1 * 60 * 60L   //  hours * minutes * seconds


    fun createConfigSettings(): FirebaseRemoteConfig? {
         val remoteConfig = runCatching { Firebase.remoteConfig }.getOrNull()

        val configSettings = /*FirebaseRemoteConfigSettings.Builder()
//            .setDeveloperModeEnabled(!BuildConfig.DEBUG)
            .setMinimumFetchIntervalInSeconds(5)
            .build()*/
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = FETCH_TIME_INTERVAL
            }
        remoteConfig?.setConfigSettingsAsync(configSettings)
        remoteConfig?.setDefaultsAsync(
            mapOf(
                MAIN_INTER_PRIORITY to false,
                SPLASH_INTER_PRIORITY to false,
                BOARDING_NATIVE_PRIORITY to false,
                LANGUAGE_NATIVE_PRIORITY to true,
                INTRUDER_NATIVE_PRIORITY to false,
                LOCKSCREEN_NATIVE_PRIORITY to false,
                MAIN_NATIVE_PRIORITY to false,
                THEME_NATIVE_PRIORITY to false,
                COMO_NATIVE_PRIORITY to false,
                SHOW_BILLING_SCREEN to false,
                MAIN_BANNER_PRIORITY to 0,
                LOCK_SCREEN_BANNER to 0,
                COLLAPSIBLE_BANNER_SHOW to false,
                Pro_Button_Main_SHOW to true,
                SHOW_DELAY_TIMER_TIME to 20,
                NOTIFICATION_ICON to 3,
            )
        )
        return remoteConfig
    }
}

const val SPLASH_INTER_PRIORITY = "splash_inter_enabling"
const val MAIN_INTER_PRIORITY = "main_inter_enabling"
const val BOARDING_NATIVE_PRIORITY = "boarding_native_enabling"
const val INTRUDER_NATIVE_PRIORITY = "intruder_native_ads_priority"
const val MAIN_NATIVE_PRIORITY = "main_native_ads_priority"
const val LOCKSCREEN_NATIVE_PRIORITY = "lockscreen_native_ads_priority"
const val THEME_NATIVE_PRIORITY = "theme_native_enabling"
const val COMO_NATIVE_PRIORITY = "como_native_enabling"
const val LANGUAGE_NATIVE_PRIORITY = "language_native_enabling"
const val MAIN_BANNER_PRIORITY = "main_banner_enabling"
const val NOTIFICATION_ICON = "notification_icon"
const val COLLAPSIBLE_BANNER_SHOW = "collapsible_banner_show"
const val Pro_Button_Main_SHOW = "pro_button_main_show"
const val LOCK_SCREEN_BANNER = "lock_screen_banner"
const val MEDIA_SCREEN_BANNER = "media_screen_banner"
const val SHOW_DELAY_TIMER_TIME = "show_delay_timer_time"
const val SHOW_BILLING_SCREEN = "show_billing_screen"


fun String.getIntRemotely() = kotlin.runCatching {
    FirebaseRemoteConfig.getInstance().getLong(this).toInt()

}.getOrNull()

fun String.getBooleanRemotely() = kotlin.runCatching {
    FirebaseRemoteConfig.getInstance().getBoolean(this)
}.getOrNull()
fun getResetRemotely() = kotlin.runCatching {
    FirebaseRemoteConfig.getInstance().reset()
}.getOrNull()

