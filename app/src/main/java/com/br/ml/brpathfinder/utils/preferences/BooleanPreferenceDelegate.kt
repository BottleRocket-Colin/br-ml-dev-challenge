package com.br.ml.brpathfinder.utils.preferences

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class BooleanPreferenceDelegate(private val prefs: SharedPreferences?) :
    ReadWriteProperty<Any, Boolean> {

    override fun getValue(thisRef: Any, property: KProperty<*>) =
        prefs?.getBoolean(property.name, false) ?: false

    override fun setValue(thisRef: Any, property: KProperty<*>, value: Boolean) {
        prefs?.edit()?.apply {
            putBoolean(property.name, value)
            apply()
        }
    }
}