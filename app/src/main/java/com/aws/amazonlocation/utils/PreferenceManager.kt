package com.aws.amazonlocation.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.aws.amazonlocation.data.enum.AuthEnum

// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.

// SPDX-License-Identifier: MIT-0
class PreferenceManager(private val appContext: Context) {

    private val mSharedPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(appContext)

    /**
     * Set a value for the key
     */
    fun setValue(key: String, value: String) {
        val editor = mSharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * Set a value for the key
     */
    fun setValue(key: String, value: Int) {
        val editor = mSharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * Set a value for the key
     */
    fun setValue(key: String, value: Double) {
        setValue(key, value.toString())
    }

    /**
     * Set a value for the key
     */
    fun setValue(key: String, value: Long) {
        val editor = mSharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun setValue(key: String, value: Set<String>) {
        val editor = mSharedPreferences.edit()
        editor.putStringSet(key, value)
        editor.apply()
    }

    /**
     * Gets the value from the settings stored natively on the device.
     *
     * @param defaultValue Default value for the key, if one is not found.
     */
    fun getValue(key: String, defaultValue: String): String? {
        return mSharedPreferences.getString(key, defaultValue)
    }

    fun getIntValue(key: String, defaultValue: Int): Int {
        return mSharedPreferences.getInt(key, defaultValue)
    }

    fun getLongValue(key: String, defaultValue: Long): Long {
        return mSharedPreferences.getLong(key, defaultValue)
    }

    fun getBooleanValue(key: String, defaultValue: Boolean): Boolean {
        return mSharedPreferences.getBoolean(key, defaultValue)
    }

    /**
     * Gets the value from the preferences stored natively on the device.
     *
     * @param defValue Default value for the key, if one is not found.
     */
    fun getValue(key: String, defValue: Boolean): Boolean {
        return mSharedPreferences.getBoolean(key, defValue)
    }

    fun setValue(key: String, value: Boolean) {
        val editor = mSharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**
     * Removes preference entry for the given key.
     *
     * @param key Value for the key
     */
    @SuppressLint("CommitPrefEdits")
    fun removeValue(key: String) {
        mSharedPreferences.edit().remove(key).apply()
    }

    fun setDefaultConfig() {
        setValue(KEY_POOL_ID, "")
        setValue(KEY_USER_POOL_ID, "")
        setValue(KEY_USER_POOL_CLIENT_ID, "")
        setValue(KEY_CLOUD_FORMATION_STATUS, AuthEnum.DEFAULT.name)
        setValue(KEY_USER_REGION, "")
        setValue(KEY_USER_DOMAIN, "")
    }
}
