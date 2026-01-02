package com.ahmetaliasik.signalpowerdetector.core.telephonyManager

import android.content.Context
import android.os.Build
import android.telephony.CellSignalStrengthGsm
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.CellSignalStrengthWcdma
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi

class SignalStrengthObserver(
    private val context: Context,
    private val onSignalChanged: (Int) -> Unit
) {
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private var modernCallback : Any? = null

    private var legacyListener : PhoneStateListener? = null

    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startModernListening()
        } else {
            startLegacyListening()
        }
    }

    fun stop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            stopModernListening()
        } else {
            stopLegacyListening()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startModernListening(){
        val callBack = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                val strength = signalStrength.cellSignalStrengths.firstOrNull()

                val dbm = when(strength) {
                    is CellSignalStrengthLte -> strength.rsrp
                    is CellSignalStrengthNr -> strength.ssRsrp
                    is CellSignalStrengthWcdma -> strength.dbm
                    is CellSignalStrengthGsm -> strength.dbm
                    else -> null
                }

                dbm?.let {
                    val percent = rsrpToPercent(it)
                    onSignalChanged(percent)
                }
            }

        }

        modernCallback = callBack

        telephonyManager.registerTelephonyCallback(
            context.mainExecutor,
            callBack
        )
    }
    @RequiresApi(Build.VERSION_CODES.S)
    private fun stopModernListening(){
        modernCallback?.let {
            if (it is TelephonyCallback) {
                telephonyManager.unregisterTelephonyCallback(it)
            }
        }
        modernCallback = null
    }

    @Suppress("DEPRECATION")
    private fun startLegacyListening() {
        legacyListener = object : PhoneStateListener() {
            override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
                super.onSignalStrengthsChanged(signalStrength)
                signalStrength?.let {
                    val level = it.level
                    val percent = ((level * 100) / 4).coerceIn(0, 100)
                    onSignalChanged(percent)
                }
            }
        }

        telephonyManager.listen(
            legacyListener,
            PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
        )
    }
    @Suppress("DEPRECATION")
    private fun stopLegacyListening() {
        legacyListener?.let {
            telephonyManager.listen(it, PhoneStateListener.LISTEN_NONE)
        }
        legacyListener = null
    }

    private fun rsrpToPercent(rsrp: Int): Int {
        return ((rsrp + 120) * 100 / 40).coerceIn(0, 100)
    }

}
