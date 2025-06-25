package com.example.smartkey

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView // Assicurati di importare CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Assicurati che CardView sia inizializzato se necessario,
        // anche se per questo esempio l'onClick è gestito direttamente nell'XML.
        val digitalCardView: CardView = findViewById(R.id.digitalCardView)
        // Non è strettamente necessario un listener qui se usi android:onClick nell'XML,
        // ma è buona pratica in un'app più complessa.
        // digitalCardView.setOnClickListener { onDigitalCardClick(it) }
    }

    // Metodo chiamato quando si clicca sulla CardView
    fun onDigitalCardClick(view: View) {
        // Mostra il popup con la chiave digitale
        showDigitalKeyPopup()
    }

    private fun showDigitalKeyPopup() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // Rimuove la barra del titolo predefinita
        dialog.setCancelable(true) // Permette di chiudere il dialog toccando fuori
        dialog.setContentView(R.layout.dialog_key_display) // Imposta il layout XML per il dialog

        // Imposta uno sfondo trasparente per il dialog per vedere gli angoli arrotondati
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val closeButton: Button = dialog.findViewById(R.id.closeButton)

        // Imposta un ID chiave di esempio. In un'app reale, questo sarebbe dinamico.


        closeButton.setOnClickListener {
            dialog.dismiss() // Chiudi il dialog quando il bottone viene cliccato
        }

        // Mostra un toast per feedback (opzionale)
        Toast.makeText(this, "Chiave digitale visualizzata!", Toast.LENGTH_SHORT).show()

        dialog.show() // Mostra il dialog
    }
}
