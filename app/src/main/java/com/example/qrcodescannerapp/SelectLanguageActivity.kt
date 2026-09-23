package com.example.qrcodescannerapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SelectLanguageActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_language)

        prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        radioGroup = findViewById(R.id.radioGroupLanguages)

        // Pehle se selected language ko check karo
        val savedLangCode = prefs.getString("selected_language", "en")
        selectSavedLanguage(savedLangCode)

        val btnConfirm = findViewById<android.widget.ImageView>(R.id.btnConfirm)
        btnConfirm.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId != -1) {
                val selectedRadioButton = findViewById<RadioButton>(selectedId)
                val langCode = selectedRadioButton.tag.toString()

                // Language save karo
                prefs.edit().putString("selected_language", langCode).apply()

                // App locale apply karo aur MainActivity restart karo
                setLocale(langCode)
                restartApp()
            }
        }
    }

    private fun selectSavedLanguage(langCode: String?) {
        val radioButtonId = when (langCode) {
            "ar" -> R.id.rbArabic
            "hi" -> R.id.rbHindi
            "bn" -> R.id.rbBengali
            "ja" -> R.id.rbJapanese
            "zh" -> R.id.rbChinese
            else -> R.id.rbEnglish
        }
        radioGroup.check(radioButtonId)
    }

    private fun setLocale(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}