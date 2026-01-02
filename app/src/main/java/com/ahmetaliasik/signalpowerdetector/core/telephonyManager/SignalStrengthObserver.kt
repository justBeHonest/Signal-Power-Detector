package com.ahmetaliasik.signalpowerdetector.core.telephonyManager

import android.content.Context
import android.telephony.CellSignalStrengthGsm
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.CellSignalStrengthWcdma
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager

class SignalStrengthObserver(
    private val context: Context,
    private val onSignalChanged: (Int) -> Unit
) {
    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val callback =
        object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {

            override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {

                val strength = signalStrength.cellSignalStrengths.firstOrNull()

                val dbm = when (strength) {
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

    fun start() {
        telephonyManager.registerTelephonyCallback(
            context.mainExecutor,
            callback
        )
    }

    fun stop() {
        telephonyManager.unregisterTelephonyCallback(callback)
    }

    fun rsrpToPercent(rsrp: Int): Int {
        return ((rsrp + 120) * 100 / 40).coerceIn(0, 100)
    }

}
