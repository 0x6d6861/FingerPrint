package co.heri.fingerprint

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.support.v4.content.ContextCompat
import android.widget.ImageView
import android.widget.TextView
import co.heri.fingerprint.utils.FingerPrintHandle
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.security.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.security.cert.CertificateException

class MainActivity : AppCompatActivity() {

    lateinit var fingerPrintManager: FingerprintManager
    lateinit var keyGuardManager: KeyguardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            fingerPrintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            keyGuardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager


            if(!fingerPrintManager.isHardwareDetected){
                // Fingerprint not detected
                update("Fingerprint sensor not detected");
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
                // Permission denied
                update("Permission denied");
            } else if (!keyGuardManager.isKeyguardSecure) {
                // Add lock method to your device
                update("Add Lock Method in your settings")
            } else if (!fingerPrintManager.hasEnrolledFingerprints()){
                // Please add atleast one finger print to your device
                update("Please add at least one fingerprint")
            } else {
                // the device is ready for scaning

                update("Place your finger on the sensor")

                generateKey()

                if(cipherInit()){
                    val cryptoObject = FingerprintManager.CryptoObject(cipher)

                    val fingerPintHandler = FingerPrintHandle(this)
                    fingerPintHandler.startAuth(fingerPrintManager, cryptoObject)
                }
            }
        } else {
            // Device android version not supported

        }
    }

    private fun update(message: String) {
        message_txt.setTextColor(Color.BLACK)
        message_txt.text = message;
    }


    private lateinit var keyStore: KeyStore; // Generates random keys for Authentication
    private lateinit var cipher: Cipher;
    private val KEY_NAME: String = "AndroidKey";


    @TargetApi(Build.VERSION_CODES.M)
    private fun generateKey() {

        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore")
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            keyStore.load(null)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                        KeyProperties.ENCRYPTION_PADDING_PKCS7
                    )
                    .build()
            )
            keyGenerator.generateKey()

        } catch (e: KeyStoreException) {

            e.printStackTrace()

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    fun cipherInit(): Boolean {
        try {
            cipher =
                Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }


        try {

            keyStore.load(
                null
            )

            val key = keyStore.getKey(KEY_NAME, null) as SecretKey

            cipher.init(Cipher.ENCRYPT_MODE, key)

            return true

        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }

    }
}
