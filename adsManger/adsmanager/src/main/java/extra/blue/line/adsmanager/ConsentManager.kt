package extra.blue.line.adsmanager


import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @param testDeviceHashedId to get DeviceHashId filter `setTestDeviceIds` in the logcat
 * @param remoteConfigKey a Firebase Remote Config parameter key with a boolean parameter value.
 * */
class ConsentManager(
    private val context: Activity,
    testDeviceHashedId: String? = null,
    remoteConfigKey: String? = null,
    var  loed: (() -> Unit)? = null,
    var  error2: (() -> Unit)? = null
) {
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val _consentState: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val consentState = _consentState.asStateFlow()

    val consentForm: ConsentForm? get() = (consentState.value as? State.Loaded)?.form

    private val debugSettings = testDeviceHashedId?.let {
        ConsentDebugSettings.Builder(context)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId(it)
            .build()
    }

    /**
     *  Consent params builder.
     *
     *  Set tag for under age of consent. false means users are not under age.
     *  - setTagForUnderAgeOfConsent(false)
     */
    private val params = ConsentRequestParameters
        .Builder()
        .setConsentDebugSettings(debugSettings)
        .setTagForUnderAgeOfConsent(false)
        .build()

    /**
     * Contains information about user's consent.
     * - [ConsentInformation.ConsentStatus].
     * - [ConsentInformation.canRequestAds].
     * */
    val information: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    val isEnableRemotely = runCatching {
        Firebase.remoteConfig.getBoolean(remoteConfigKey!!)
    }.getOrDefault(true)

    init {
        if (isEnableRemotely) {
            information.requestConsentInfoUpdate(context, params, {
                loadConsentFormIfRequired(context)
            }, {
                _consentState.value = State.Failed(it)
                Log.e("mytage ", "${it} : ", )
                error2?.invoke()
            })
        } else {
            _consentState.value = State.Failed(null)
            Log.e("mytage ", "${null} : ", )
            error2?.invoke()
        }
    }

    /**
     * Loads a consent form. Must be called on the main thread.
     * */
    private fun loadConsentFormIfRequired(context: Activity) {

        if (information.canRequestAds()) {
            initializeMobileAdsSdk()
            _consentState.value = State.Failed(null)
            Log.e("mytage ", "${null} : ", )

            error2?.invoke()

        } else {

            UserMessagingPlatform.loadConsentForm(context, {
                _consentState.value = State.Loaded(it)
                loed?.invoke()
            }, { error ->
                error2?.invoke()
                _consentState.value = State.Failed(error)
                Log.e("mytage ", "${error} : ", )

            })

        }
    }

    /**
     * In testing your app with the UMP SDK, you might find it helpful to reset the
     * state of the SDK so that you can simulate a user's first install experience.
     * */
    fun reset() {
        information.reset()
    }

    sealed interface State {
        object Loading : State
        data class Loaded(val form: ConsentForm) : State
        data class Failed(val error: FormError? = null) : State
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(false)) {
            return;
        }

        // Initialize the Google Mobile Ads SDK.
        MobileAds.initialize(context);

        // TODO: Request an ad.
        // InterstitialAd.load(...);
    }

}
