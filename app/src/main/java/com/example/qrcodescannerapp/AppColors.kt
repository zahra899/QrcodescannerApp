package com.example.qrcodescannerapp

import androidx.core.content.ContextCompat
import android.content.Context

object AppColors {
    fun getBackground(context: Context) = ContextCompat.getColor(context, R.color.qr_background)
    fun getSurface(context: Context) = ContextCompat.getColor(context, R.color.qr_surface)
    fun getPrimary(context: Context) = ContextCompat.getColor(context, R.color.qr_primary)
    fun getTextPrimary(context: Context) = ContextCompat.getColor(context, R.color.text_primary)
    fun getTextSecondary(context: Context) = ContextCompat.getColor(context, R.color.text_secondary)

    fun getIconUrl(context: Context) = ContextCompat.getColor(context, R.color.icon_url)
    fun getIconWifi(context: Context) = ContextCompat.getColor(context, R.color.icon_wifi)
    fun getIconContact(context: Context) = ContextCompat.getColor(context, R.color.icon_contact)
    fun getIconPhone(context: Context) = ContextCompat.getColor(context, R.color.icon_phone)
    fun getIconEmail(context: Context) = ContextCompat.getColor(context, R.color.icon_email)
    fun getIconText(context: Context) = ContextCompat.getColor(context, R.color.icon_text)
}