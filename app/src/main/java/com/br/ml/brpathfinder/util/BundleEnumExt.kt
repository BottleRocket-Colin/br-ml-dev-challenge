package com.br.ml.brpathfinder.util

import android.os.Bundle

fun Bundle.putEnum(key:String, enum: Enum<*>){
    putString(key, enum.name)
}

inline fun <reified T: Enum<T>> Bundle.getEnum(key:String): T {
    return java.lang.Enum.valueOf(T::class.java, getString(key))
}