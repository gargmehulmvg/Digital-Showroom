package com.digitaldukaan.constants

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoroutineScopeUtils {
    fun runTaskOnCoroutineMain(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.Main).launch {
            try {
                work()
            } catch (e: Exception) {
                Log.e("CoroutineScopeUtils", "runTaskOnCoroutineMain: ${e.message}", e)
            }
        }

    fun runTaskOnCoroutineBackground(work: suspend (() -> Unit)) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                work()
            } catch (e: Exception) {
                Log.e("CoroutineScopeUtils", "runTaskOnCoroutineBackground: ${e.message}", e)
            }
        }
}