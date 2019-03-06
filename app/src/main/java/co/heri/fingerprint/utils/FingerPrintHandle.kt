package co.heri.fingerprint.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.widget.ImageView
import android.widget.TextView
import co.heri.fingerprint.R


@TargetApi(Build.VERSION_CODES.M)
class FingerPrintHandle(val context: Context): FingerprintManager.AuthenticationCallback() {

    fun startAuth(fingerPrintManager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject?){
        val cancellationSignal = CancellationSignal()
        fingerPrintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null )
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
        super.onAuthenticationSucceeded(result)
        this.update("Authentication is successful!!", true)
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        super.onAuthenticationError(errorCode, errString)
        this.update("There was an error in authentication $errString", false )
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        this.update("Authentication failed ", false )

    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        super.onAuthenticationHelp(helpCode, helpString)
        this.update("Error: $helpString", false)
    }

    private fun update(message: String, success: Boolean) {

        val response_message = (this.context as Activity).findViewById<TextView>(R.id.message_txt)

        var fingerImage = (this.context as Activity).findViewById<ImageView>(R.id.scanImage)

        response_message.text = message;

        if(!success) {
            response_message.setTextColor(Color.RED)
            fingerImage.drawable.setTint(Color.RED)
        } else {
            fingerImage.drawable.setTint(Color.GREEN)
            response_message.setTextColor(Color.BLUE)
        }


    }

}
