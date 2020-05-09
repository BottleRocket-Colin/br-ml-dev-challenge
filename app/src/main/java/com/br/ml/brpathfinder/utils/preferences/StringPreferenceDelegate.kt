package com.br.ml.brpathfinder.utils.preferences

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StringPreferenceDelegate(private val prefs: SharedPreferences?) :
    ReadWriteProperty<Any, String> {

    override fun getValue(thisRef: Any, property: KProperty<*>) =
        prefs?.getString(property.name, "") ?: ""

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String) {
        prefs?.edit()?.apply {
            putString(property.name, value)
            apply()
        }
    }
}