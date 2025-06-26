package com.example.smartkey

import android.app.Dialog
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var nfcStatusTextView: TextView // TextView per lo stato NFC
    private lateinit var cardStatusText: TextView // TextView all'interno della CardView principale
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFilters: Array<IntentFilter>

    // Variabile per memorizzare l'UID dell'ultima chiave riconosciuta
    private var unlockedKeyUid: ByteArray? = null

    // UID del tag/telefono che si desidera riconoscere.
    // Sostituisci questi con gli UID reali dei tuoi telefoni/tag.
    // Puoi ottenere l'UID leggendolo tramite un'app NFC o dal tuo sketch Arduino.
    private val knownUids = listOf(
        byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte()), // Esempio UID 1
        byteArrayOf(0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte())  // Esempio UID 2
    )

    private val TAG = "SmartKeyApp" // TAG per i messaggi di log

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nfcStatusTextView = findViewById(R.id.nfcStatusTextView)
        cardStatusText = findViewById(R.id.cardStatusText) // Inizializza la TextView interna alla card

        // Inizializzazione NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            nfcStatusTextView.text = "NFC non supportato su questo dispositivo."
            Toast.makeText(this, "NFC non supportato.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "NFC non supportato su questo dispositivo.")
        } else if (!nfcAdapter!!.isEnabled) {
            nfcStatusTextView.text = "NFC disabilitato. Abilitalo dalle impostazioni."
            Toast.makeText(this, "NFC disabilitato. Abilitalo dalle impostazioni.", Toast.LENGTH_LONG).show()
            Log.w(TAG, "NFC disabilitato. Richiesta abilitazione all'utente.")
            // Opzionale: indirizza l'utente alle impostazioni NFC
            // startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        } else {
            nfcStatusTextView.text = "Avvicina il telefono al sensore NFC per sbloccare."
            cardStatusText.text = "Status: In attesa di scansione"
            Log.i(TAG, "NFC è disponibile e abilitato. In attesa di tag...")
        }

        // Configura il Foreground Dispatch System per intercettare gli intent NFC
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        // Puoi aggiungere altri filtri se necessario, come NDEF_DISCOVERED
        intentFilters = arrayOf(tagDetected)
    }

    override fun onResume() {
        super.onResume()
        // Abilita il foreground dispatch quando l'app è in primo piano.
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFilters, null)
        Log.d(TAG, "Foreground dispatch abilitato.")
    }

    override fun onPause() {
        super.onPause()
        // Disabilita il foreground dispatch quando l'app non è più in primo piano.
        nfcAdapter?.disableForegroundDispatch(this)
        Log.d(TAG, "Foreground dispatch disabilitato.")
    }

    // Questo metodo viene chiamato quando un nuovo tag NFC viene rilevato
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action || NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                val detectedUidBytes = tag.id // L'UID del tag letto come ByteArray
                val detectedUidHexString = bytesToHexString(detectedUidBytes) // Converti in stringa esadecimale

                Log.d(TAG, "UID del tag rilevato: $detectedUidHexString")

                if (isUidKnown(detectedUidBytes)) {
                    unlockedKeyUid = detectedUidBytes // Memorizza l'UID riconosciuto
                    nfcStatusTextView.text = "UID riconosciuto: ${detectedUidHexString}\nPortiere sbloccate! (Simulazione)"
                    cardStatusText.text = "Status: Chiave disponibile"
                    Toast.makeText(this, "Portiere sbloccate! Chiave pronta.", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "UID riconosciuto. Chiave memorizzata.")
                    // Potresti anche mostrare il popup automaticamente qui se preferisci
                    // showDigitalKeyPopup(unlockedKeyUid)
                } else {
                    unlockedKeyUid = null // Resetta la chiave se l'UID non è riconosciuto
                    nfcStatusTextView.text = "UID sconosciuto: ${detectedUidHexString}\nAccesso negato."
                    cardStatusText.text = "Status: Accesso negato"
                    Toast.makeText(this, "UID sconosciuto. Accesso negato.", Toast.LENGTH_SHORT).show()
                    Log.w(TAG, "UID sconosciuto. Accesso negato.")
                }
            } else {
                Log.e(TAG, "Tag NFC rilevato, ma extra EXTRA_TAG è null.")
            }
        } else {
            Log.d(TAG, "Intent NFC ricevuto con azione non gestita: ${intent.action}")
        }
    }

    // Metodo chiamato quando si clicca sulla CardView principale
    fun onDigitalCardClick(view: View) {
        if (unlockedKeyUid != null) {
            // Se un UID riconosciuto è stato scansionato, mostra il popup con quell'UID
            showDigitalKeyPopup(unlockedKeyUid!!) // Usiamo !! perché abbiamo controllato che non sia null
        } else {
            // Se nessun UID riconosciuto è stato scansionato, informa l'utente
            Toast.makeText(this, "Scansiona prima una chiave NFC riconosciuta!", Toast.LENGTH_LONG).show()
            nfcStatusTextView.text = "Avvicina il telefono a una chiave riconosciuta per sbloccare."
            Log.d(TAG, "Tentativo di aprire popup senza UID riconosciuto.")
        }
    }

    // Metodo per mostrare il popup con la chiave digitale
    private fun showDigitalKeyPopup(keyUidBytes: ByteArray) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_key_display)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val keyIdTextView: TextView = dialog.findViewById(R.id.keyIdTextView)
        val closeButton: Button = dialog.findViewById(R.id.closeButton)

        // Imposta l'UID effettivo sulla TextView del popup
        keyIdTextView.text = "KEY: ${bytesToHexString(keyUidBytes)}"

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        Toast.makeText(this, "Chiave digitale visualizzata!", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "Popup chiave digitale mostrato con UID: ${bytesToHexString(keyUidBytes)}")
        dialog.show()
    }

    // Funzione per controllare se l'UID rilevato è tra quelli conosciuti
    private fun isUidKnown(detectedUid: ByteArray): Boolean {
        // Puoi estendere questa logica se gli UID possono avere dimensioni diverse
        // o se vuoi un confronto più sofisticato (es. hash, cifratura).
        for (knownUid in knownUids) {
            if (detectedUid.contentEquals(knownUid)) {
                return true
            }
        }
        return false
    }

    // Funzione helper per convertire un array di byte in una stringa esadecimale
    private fun bytesToHexString(src: ByteArray?): String {
        if (src == null || src.isEmpty()) {
            return ""
        }
        val stringBuilder = StringBuilder("0x")
        for (b in src) {
            val hex = Integer.toHexString(0xFF and b.toInt())
            if (hex.length == 1) {
                stringBuilder.append("0")
            }
            stringBuilder.append(hex)
        }
        return stringBuilder.toString().uppercase()
    }
}
